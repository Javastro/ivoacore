package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.persist.JobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobManager implements ExecutionControl, UWSCore {

   private static final Logger log = LoggerFactory.getLogger(JobManager.class);
   private final JobStore jobStore;
   private final ExecutionEnvironment environment;
   private final JobFactory jobFactory;
   private final ExecutionPolicy executionPolicy;
   final ExecutorService executorService;

   public JobManager(ExecutionEnvironment environment, JobFactory jobFactory, JobStore jobStore, ExecutionPolicy executionPolicy) {
      this.environment = environment;
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
      throw new UWSException("Not yet implemented");
   }

   @Override
   public org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException {
      return jobStore.retrieve(jobId).asJob();
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
   public void deleteJob(String jobId) throws UWSException {
      throw new UWSException("Not yet implemented");
   }

   @Override
   public List<ParameterValue> getJobResults(String jobId) throws UWSException {
      //FIXME we really do not want to do this simplistic thing, but rather have a component that translates job results into their location
      return jobStore.retrieve(jobId).getResults();
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
