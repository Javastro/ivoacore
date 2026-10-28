package org.javastro.ivoacore.uws;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;


/**
 * A Runnable UWS Job that can be executed asynchronously.
 * This class extends {@link BaseUWSJob} and implements the {@link Job} interface,
 * providing functionality for submitting the job for execution, monitoring its progress, and handling completion or abortion of the job.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public abstract class RunnableUWSJob extends BaseUWSJob implements Job {

   /** The future representing the asynchronous execution of this job. */
   protected volatile CompletableFuture<ExecutionPhase> jobFuture;
   private volatile Future<?> executionTask;
   private volatile boolean cancellationRequested;

   /**
    * Returns the future representing the asynchronous execution of this job.
    * @return the {@link CompletableFuture} for this job's execution.
    */
   public CompletableFuture<ExecutionPhase> getJobFuture() {
      return jobFuture;
   }

   /**
    * Creates a runnable UWS job.
    *
    * @param jobID unique job identifier.
    * @param jobSpecification job configuration and parameters.
    * @param executionEnvironment execution environment for job artifacts.
    * @param executionPolicy execution policy governing job execution constraints.
    */
   protected RunnableUWSJob(String jobID, JobSpecification jobSpecification, ExecutionEnvironment executionEnvironment, ExecutionPolicy executionPolicy) {
      super(jobID, jobSpecification, executionEnvironment, executionPolicy);
   }

   /**
    * Submits this job for asynchronous execution using the given executor service.
    * @param executorService the executor service to use for running the job.
    */
   synchronized void submitJobToRun(ExecutorService executorService)  {
      if (executionPhase != ExecutionPhase.PENDING) {
         throw new IllegalStateException("Job " + jobID + " is not pending");
      }
      cancellationRequested = false;
      jobFuture = new CompletableFuture<>();
      executionTask = executorService.submit(() -> {
         try {
            ExecutionPhase phase = getJobCallable().get();
            jobFuture.complete(phase);
            logger.info("Job {} completed with phase {}", jobID, phase);
         } catch (RuntimeException | Error e) {
            jobFuture.completeExceptionally(e);
            throw e;
         }
      });
   }

   /**
    * Aborts this job.
    */
   public synchronized void abort() {
      cancel(ExecutionPhase.ABORTED, "aborted");
   }

   /**
    * Requests interruption of this job after it exceeds its execution duration.
    */
   public synchronized void abortForExecutionTimeout() {
      cancel(ExecutionPhase.ABORTED, "exceeded its execution duration");
   }

   private void cancel(ExecutionPhase terminalPhase, String reason) {
      if (executionPhase == ExecutionPhase.COMPLETED
            || executionPhase == ExecutionPhase.ERROR
            || executionPhase == ExecutionPhase.ABORTED) {
         return;
      }
      cancellationRequested = true;
      Future<?> task = executionTask;
      if (task != null) {
         task.cancel(true);
      }
      executionPhase = terminalPhase;
      endTime = ZonedDateTime.now(ZoneId.of("UTC"));
      CompletableFuture<ExecutionPhase> future = jobFuture;
      if (future != null) {
         future.complete(terminalPhase);
      }
      logger.info("Job {} {}", jobID, reason);
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
      jobFuture.join();
   }


   private Supplier<ExecutionPhase> getJobCallable() {
      return () -> {
         synchronized (this) {
            if (cancellationRequested || executionPhase != ExecutionPhase.PENDING) {
               return executionPhase;
            }
            logger.info("Starting job execution of {}", jobID);
            startTime = ZonedDateTime.now(ZoneId.of("UTC"));
            executionPhase = ExecutionPhase.EXECUTING;
         }
         try {
            List<ParameterValue> jobResults = performAction();
            synchronized (this) {
               if (!cancellationRequested) {
                  results = jobResults;
                  executionPhase = ExecutionPhase.COMPLETED;
                  endTime = ZonedDateTime.now(ZoneId.of("UTC"));
               }
               logger.info("Finished execution of {}", jobID);
               return executionPhase;
            }
         }
         catch (UWSException e) {
            synchronized (this) {
               if (!cancellationRequested) {
                  logger.error("Error during execution of job {}: {}\n", jobID, e.getMessage(), e);
                  executionPhase = ExecutionPhase.ERROR;
                  endTime = ZonedDateTime.now(ZoneId.of("UTC"));
                  exception = e;
               }
               return executionPhase;
            }
         }
         catch (RuntimeException e) {
            synchronized (this) {
               if (!cancellationRequested) {
                  UWSException jobException = new UWSException("Unexpected error during job execution", e);
                  logger.error("Error during execution of job {}", jobID, e);
                  executionPhase = ExecutionPhase.ERROR;
                  endTime = ZonedDateTime.now(ZoneId.of("UTC"));
                  exception = jobException;
               }
               throw e;
            }
         }

      };
   }


}
