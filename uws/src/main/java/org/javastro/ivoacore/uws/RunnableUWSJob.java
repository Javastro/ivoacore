package org.javastro.ivoacore.uws;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;


/**
 * A Runnable UWS Job that can be executed asynchronously.
 * This class extends {@link BaseUWSJob} and implements the {@link Job} interface,
 * providing functionality for submitting the job for execution, monitoring its progress, and handling completion or abortion of the job.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public abstract class RunnableUWSJob extends BaseUWSJob implements Job {

   /** The future representing the asynchronous execution of this job. */
   protected CompletableFuture<ExecutionPhase> jobFuture;

   /**
    * Returns the future representing the asynchronous execution of this job.
    * @return the {@link CompletableFuture} for this job's execution.
    */
   public CompletableFuture<ExecutionPhase> getJobFuture() {
      return jobFuture;
   }



   protected RunnableUWSJob(String jobID, JobSpecification jobSpecification, ExecutionEnvironment executionEnvironment) {
      super(jobID, jobSpecification, executionEnvironment);
   }

   /**
    * Submits this job for asynchronous execution using the given executor service.
    * @param executorService the executor service to use for running the job.
    */
   void submitJobToRun(ExecutorService executorService)  {
      jobFuture = CompletableFuture.supplyAsync(getJobCallable(),executorService).thenApply(e ->{logger.info("Job {} completed with phase {}", jobID, e); return e;});//TODO perhaps the API should really deal with the CompletableFuture, with that stored in the JobStore
   }

   /**
    * Aborts this job.
    */
   public void abort() {
      //if it has not started just set the phase to aborted and end time to now, otherwise try to cancel the future
      boolean cancelled = jobFuture!= null?jobFuture.cancel(true):true;//TODO need to set the status when this happens
      if (cancelled) {
         logger.info("Job {} aborted", jobID);
         executionPhase = ExecutionPhase.ABORTED;
         endTime = ZonedDateTime.now(ZoneId.of("UTC"));
      }
      else  {
         executionPhase = ExecutionPhase.UNKNOWN;
         logger.error("Failed top abort job {}", jobID);
      }
   }

   /**
    * This is a blocking call to wait for the job to finish - either successfully or not.
    * Use with caution as it will block the calling thread until the job completes.
    * @see #getJobFuture() for a non-blocking way to monitor job completion.
    * @throws IllegalStateException if the job has not been submitted for execution yet.
    */
   public void blockingWaitForFinish() {
      if (jobFuture == null) {
         throw new IllegalStateException("Job " + jobID + " has not been submitted for execution");
      }
      try {
         jobFuture.join(); // blocks until the future completes (normally or exceptionally)
      } catch (java.util.concurrent.CancellationException e) {
         logger.warn("Job {} was cancelled while waiting for finish", jobID);
      }
   }


   private Supplier<ExecutionPhase> getJobCallable() {
      return () -> {
         logger.info("Starting job execution of {}", jobID);
         executionPhase = ExecutionPhase.EXECUTING;
         startTime = ZonedDateTime.now(ZoneId.of("UTC"));
         try {
            results = performAction();
            executionPhase = ExecutionPhase.COMPLETED;
            endTime = ZonedDateTime.now(ZoneId.of("UTC"));
            logger.info("Finished execution of {}", jobID);
            return executionPhase;
         }
         catch (UWSException e) {
            logger.error("Error during execution of job {}: {}\n", jobID, e.getMessage(), e);
            executionPhase = ExecutionPhase.ERROR;
            endTime = ZonedDateTime.now(ZoneId.of("UTC"));
            exception = e;
            return executionPhase;
         }

      };
   }


}
