package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoa.entities.uws.ShortJobDescription;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.persist.JobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager for UWS jobs that handles creation, execution, and lifecycle management of jobs.
 */
public class JobManager implements ExecutionControl, UWSCore, AutoCloseable {

   private static final Logger log = LoggerFactory.getLogger(JobManager.class);
   private final JobStore jobStore;
   private final JobFactory jobFactory;
   private final ExecutionPolicy executionPolicy;
   private final Map<String, RunnableUWSJob> activeJobs = new ConcurrentHashMap<>();
   private final Set<String> pendingDeletion = ConcurrentHashMap.newKeySet();
   final ExecutorService executorService;
   private final ScheduledExecutorService jobMonitor;

   /**
    * Constructs a new JobManager with the given environment, factory, store and policy.
    * @param jobFactory the factory for creating jobs.
    * @param jobStore the persistent store for job instances.
    * @param executionPolicy the policy governing execution constraints.
    */
   public JobManager(JobFactory jobFactory, JobStore jobStore, ExecutionPolicy executionPolicy) {
      this.jobFactory = jobFactory;
      this.jobStore = jobStore;
      this.executionPolicy = executionPolicy;
      if (executionPolicy.getMaxConcurrent() <= 0) {
         throw new IllegalArgumentException("Maximum concurrent jobs must be greater than zero");
      }
      if (executionPolicy.getKillPeriod() <= 0) {
         throw new IllegalArgumentException("Execution kill period must be greater than zero seconds");
      }
      if (executionPolicy.getDestroyPeriod() <= 0) {
         throw new IllegalArgumentException("Job destruction check period must be greater than zero seconds");
      }
      this.executorService = Executors.newFixedThreadPool(executionPolicy.getMaxConcurrent());
      this.jobMonitor = Executors.newScheduledThreadPool(2, task -> {
         Thread thread = new Thread(task, "uws-job-monitor");
         thread.setDaemon(true);
         return thread;
      });
      this.jobMonitor.scheduleAtFixedRate(this::checkExecutionDurations,
            executionPolicy.getKillPeriod(), executionPolicy.getKillPeriod(), TimeUnit.SECONDS);
      this.jobMonitor.scheduleAtFixedRate(this::destroyExpiredJobs,
            executionPolicy.getDestroyPeriod(), executionPolicy.getDestroyPeriod(), TimeUnit.SECONDS);
      log.info("Created JobManager");

   }

   @Override
   public RunnableUWSJob createJob(JobSpecification specification) throws UWSException {
      RunnableUWSJob retval = jobFactory.createJob(specification);
      jobStore.store(retval);
      return retval;
   }

   @Override
   public Set<String> listJobIDs() throws UWSException {
      return jobStore.getAllIds();
   }

   @Override
   public Jobs listJobs(String phase, ZonedDateTime after, Integer last) throws UWSException {
      final ExecutionPhase parsedPhase;
      if (phase == null || phase.isBlank()) {
         parsedPhase = null;
      } else {
         try {
            parsedPhase = ExecutionPhase.valueOf(phase.trim().toUpperCase());
         } catch (IllegalArgumentException e) {
            throw new UWSException("Invalid PHASE value: " + phase, e);
         }
      }

      List<ShortJobDescription> joblist = jobStore.getJobs(parsedPhase, after, last).stream()
            .map(BaseUWSJob::asShortDescription)
            .toList();

      return new Jobs(joblist, "1.1");
   }

   @Override
   public org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException {
      final BaseUWSJob job = jobStore.retrieve(jobId);
      return job!=null?job.asJob():null;
   }

   @Override
   public ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException {
      BaseUWSJob job = jobStore.retrieve(jobId);
      if(job instanceof RunnableUWSJob runnableUWSJob) {
         switch (newPhase.toUpperCase()) {//IMPL
            case "RUN":
               try {
                  submitRunnableJob(runnableUWSJob);
               } catch (RuntimeException e) {
                  if (runnableUWSJob.getExecutionPhase() == ExecutionPhase.ERROR) {
                     jobStore.store(job); //Persist a failed submission state to the job store
                     log.error("Failed to submit job {}", jobId, e);
                     break;
                  }

                  throw new UWSException("Cannot run job " + jobId + ": " + e.getMessage());
               }

               jobStore.store(job);
               break;
            case "ABORT":
               runnableUWSJob.abort();
               jobStore.store(job);
               break;
            default:
               throw new UWSException("illegal phase " + newPhase);
         }
         return runnableUWSJob.executionPhase;
      }
      else {
         log.warn("job id="+jobId+" is only present in backing store, cannot be run" );
         return job.executionPhase;
      }
   }


   @Override
   public ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException {
      BaseUWSJob job = requireJob(jobId);
      if (destructionTime == null) {
         throw new UWSException("Destruction time is required");
      }
      job.setDestructionTime(destructionTime);
      jobStore.store(job);
      return job.getDestructionTime();
   }

   @Override
   public Long setExecutionDuration(String jobId, Long duration) throws UWSException {
      BaseUWSJob job = requireJob(jobId);
      if (duration == null || duration < 0 || duration > Integer.MAX_VALUE) {
         throw new UWSException("Execution duration must be between 0 and " + Integer.MAX_VALUE + " seconds");
      }
      int maxRunTime = job.getExecutionPolicy().getMaxRunTime();
      if (maxRunTime > 0 && duration > maxRunTime) {
         throw new UWSException("Execution duration cannot exceed the policy maximum of " + maxRunTime + " seconds");
      }
      job.setExecutionDuration(duration.intValue());
      jobStore.store(job);
      return (long) job.getExecutionDuration();
   }

   private BaseUWSJob requireJob(String jobId) throws UWSException {
      BaseUWSJob job = jobStore.retrieve(jobId);
      if (job == null) {
         throw new UWSException("Job with id " + jobId + " not found");
      }
      return job;
   }

   private void submitRunnableJob(RunnableUWSJob job) {
      String jobId = job.getID();

      if (activeJobs.putIfAbsent(jobId, job) != null) {
         throw new IllegalStateException("Job is already active: " + jobId);
      }

      try {
         job.prepareForSubmission();

         job.getWorkerTermination().whenComplete((ignored, error) -> {
            synchronized (job) {
               activeJobs.remove(jobId, job);
               try {
                  if (pendingDeletion.remove(jobId)) {
                     jobStore.delete(jobId);
                  } else {
                     jobStore.store(job);
                  }
               } catch (RuntimeException e) {
                  log.error("Failed to persist final state for job {}", jobId, e);
               }
            }
         });

         //Once the job is prepared, submit it to the executor service for execution
         job.submitJobToRun(executorService);
      } catch (RuntimeException e) {
         activeJobs.remove(jobId, job);
         throw e;
      }
   }

   private void checkExecutionDurations() {
      ZonedDateTime now = ZonedDateTime.now();

      for (RunnableUWSJob job : activeJobs.values()) {
         try {
            if (job.getExecutionPhase() != ExecutionPhase.EXECUTING) {
               continue;
            }

            int jobLimit = job.getExecutionDuration();
            int policyLimit = job.getExecutionPolicy().getMaxRunTime();

            int effectiveLimit = jobLimit <= 0 ? policyLimit
                    : policyLimit <= 0 ? jobLimit
                    : Math.min(jobLimit, policyLimit);

            ZonedDateTime startTime = job.getStartTime();

            if (effectiveLimit > 0
                    && startTime != null
                    && !now.isBefore(startTime.plusSeconds(effectiveLimit))) {
               log.warn("Execution duration exceeded for job {}; requesting cancellation",
                       job.getID());
               job.abortForExecutionTimeout();
            }
         } catch (RuntimeException e) {
            log.error("Failed to check execution duration for job {}", job.getID(), e);
         }
      }
   }

   private void destroyExpiredJobs() {
      try {
         ZonedDateTime now = ZonedDateTime.now();
         for (BaseUWSJob storedJob : jobStore.getJobs(null, null, null)) { //TODO make this more efficient by only retrieving jobs with destruction times in the past
            ZonedDateTime destructionTime = storedJob.getDestructionTime();
            if (destructionTime == null || destructionTime.isAfter(now)) {
               continue;
            }

            String jobId = storedJob.getID();
            RunnableUWSJob activeJob = activeJobs.get(jobId);
            synchronized (activeJob == null ? storedJob : activeJob) {
               if (activeJob != null) {
                  pendingDeletion.add(jobId);
                  activeJob.abort();
               } else {
                  if (jobStore.delete(jobId)) {
                     log.info("Destroyed expired job {}", jobId);
                  }
               }
            }
         }
      } catch (RuntimeException e) {
         log.error("Failed to destroy expired UWS jobs", e);
      }
   }


   @Override
   public boolean deleteJob(String jobId) throws UWSException {
      BaseUWSJob thisJob = jobStore.retrieve(jobId);
      if (thisJob == null) {
         return false;
      }
      RunnableUWSJob activeJob = activeJobs.get(jobId);
      synchronized (activeJob == null ? thisJob : activeJob) {
         if (activeJob != null) {
            pendingDeletion.add(jobId);
            activeJob.abort();
         }
         return jobStore.delete(jobId);
      }
   }

   @Override
   public List<ParameterValue> getJobResults(String jobId) throws UWSException {
      //FIXME we really do not want to do this simplistic thing, but rather have a component that translates job results into their location
      return jobStore.retrieve(jobId).getResults();
   }

   @Override
   public String jobErrorDetail(String jobid) {

         BaseUWSJob job = jobStore.retrieve(jobid);
         if (job.getExecutionPhase() == ExecutionPhase.ERROR) {
            BaseUWSJob thisjob = jobStore.retrieve(jobid);
            if (thisjob.exception != null) {
               StringWriter sw = new StringWriter();
               PrintWriter pw = new PrintWriter(sw);
               thisjob.exception.printStackTrace(pw);
               pw.close();
               return sw.toString();
            }
            else {
               return "No error message available";
            }
         }
         else {
            return "No error - job is in phase " + job.getExecutionPhase();
         }
   }

   @Override
   public void runJob(String jobId) throws UWSException {
      BaseUWSJob job = jobStore.retrieve(jobId);
      if(job instanceof RunnableUWSJob runnableUWSJob) {
         submitRunnableJob(runnableUWSJob);
      }
      else {
         throw new UWSException("cannot run job  "+ jobId + " it does not exist in main memory - copy and recreate job");
      }

   }

   @Override
   public void abortJob(String jobId) throws UWSException {
      BaseUWSJob job = jobStore.retrieve(jobId);
      if(job instanceof RunnableUWSJob runnableUWSJob)
      {
         runnableUWSJob.abort();
         jobStore.store(job);
      }
      else {
         log.warn("attempt to abort a job that is not running, jobid="+jobId);
      }
   }

   /**
    * Stops the manager's execution monitor and worker pool.
    */
   @Override
   public void close() {
      jobMonitor.shutdownNow();
      executorService.shutdownNow();
   }
}
