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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager for UWS jobs that handles creation, execution, and lifecycle management of jobs.
 */
public class JobManager implements ExecutionControl, UWSCore {

   private static final Logger log = LoggerFactory.getLogger(JobManager.class);
   private final JobStore jobStore;
   private final JobFactory jobFactory;
   private final ExecutionPolicy executionPolicy;
   final ExecutorService executorService;

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
      this.executorService = Executors.newFixedThreadPool(executionPolicy.getMaxConcurrent());
      log.info("Created JobManager");

   }

   @Override
   public BaseUWSJob createJob(JobSpecification specification) throws UWSException {
      BaseUWSJob retval = jobFactory.createJob(specification);
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
      switch (newPhase.toUpperCase()) {//IMPL
         case "RUN":
            job.submitJobToRun(executorService);
            break;
         case "ABORT":
            job.abort();
            break;
         default:
            throw new UWSException("illegal phase " + newPhase);
      }
      return job.executionPhase;
   }


   @Override
   public ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException {
      throw new UWSException("Not yet implemented");
   }

   @Override
   public Long setExecutionDuration(String jobId, Long duration) throws UWSException {
      throw new UWSException("Not yet implemented");
   }


   @Override
   public boolean deleteJob(String jobId) throws UWSException {
      BaseUWSJob thisjob = jobStore.retrieve(jobId);
      thisjob.abort();
      return jobStore.delete(jobId);
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
      job.submitJobToRun(executorService);
   }

   @Override
   public void abortJob(String jobId) throws UWSException {
      jobStore.retrieve(jobId).abort();
   }
}
