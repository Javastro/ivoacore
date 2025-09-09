package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.persist.JobStore;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobManager implements ExecutionControl {

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
   }

   @Override
   public BaseUWSJob createJob(JobSpecification specification) throws UWSException {
      BaseUWSJob retval = jobFactory.createJob(specification);
      jobStore.store(retval);
      return retval;
   }

   @Override
   public Set<String> listJobs() throws UWSException {
      return jobStore.getAllIds();
   }

   @Override
   public org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException {
      return null;
   }

   @Override
   public ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException {
      return null;
   }

   @Override
   public ExecutionPhase getPhase(String jobId) throws UWSException {
      return jobStore.retrieve(jobId).getExecutionPhase();
   }

   @Override
   public ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException {
      return null;
   }

   @Override
   public Duration setExecutionDuration(String jobId, Duration time) throws UWSException {
      return null;
   }

   @Override
   public void deleteJob(String jobId) throws UWSException {

   }

   @Override
   public Results getResults(String jobId) throws UWSException {
      return null;
   }

   @Override
   public void runJob(String jobId) throws UWSException {
      BaseUWSJob job = jobStore.retrieve(jobId);
      job.submitJobToRun(executorService);
   }

   @Override
   public void abortJob(String jobId) throws UWSException {

   }
}
