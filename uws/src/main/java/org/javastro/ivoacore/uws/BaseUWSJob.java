/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.*;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * A UWS Job.
 */
public abstract class BaseUWSJob implements Job { //IMPL should probably pull some of the methods in this class
   private static final Logger logger = LoggerFactory.getLogger(BaseUWSJob.class.getName());
   /** The unique identifier for this job. */
   private final String jobID;
   /** The current execution phase of this job. */
   protected ExecutionPhase executionPhase; //TODO think about thread safety of executionPhase - should it be volatile? should we use an AtomicReference? should we synchronize access to it?
   /** The time at which this job was created. */
   protected ZonedDateTime creationTime;
   /** The time at which this job started executing. */
   protected ZonedDateTime startTime;
   /** The time at which this job finished executing. */
   protected ZonedDateTime endTime;
   /** The specification describing this job's parameters and type. */
   protected final JobSpecification jobSpecification;
   /** The list of result parameter values produced by this job. */
   protected List<ParameterValue> results = new ArrayList<>();
   /** The future representing the asynchronous execution of this job. */
   protected CompletableFuture<ExecutionPhase> jobFuture;
   /** Any exception that occurred during the execution of this job. */
   protected UWSException exception;


   /**
    * Constructs a new BaseUWSJob with the given job ID and specification.
    * @param jobID the unique identifier for this job.
    * @param jobSpecification the specification describing the job's parameters and type.
    */
   protected BaseUWSJob(String jobID, JobSpecification jobSpecification, ExecutionEnvironment executionEnvironment) {
      this.jobSpecification = jobSpecification;
      this.jobID = jobID;
      this.executionEnvironment = executionEnvironment;
      this.executionPhase = ExecutionPhase.PENDING;
      this.creationTime = ZonedDateTime.now(ZoneId.of("UTC"));
   }

   /** The execution environment for this job. */
   protected final ExecutionEnvironment executionEnvironment;

   /**
    * Returns the future representing the asynchronous execution of this job.
    * @return the {@link CompletableFuture} for this job's execution.
    */
   public CompletableFuture<ExecutionPhase> getJobFuture() {
      return jobFuture;
   }

   /**
    * Returns the unique identifier for this job.
    * @return the job ID string.
    */
   public String getID() {
      return jobID;
   }

   /**
    * Returns the specification describing this job.
    * @return the {@link JobSpecification} for this job.
    */
   public JobSpecification getJobSpecification() {
      return jobSpecification;
   }

   /**
    * Returns the list of result parameter values produced by this job.
    * @return the list of result {@link ParameterValue} objects.
    */
   public List<ParameterValue> getResults() {
      return results;
   }

   /**
    * Returns the current execution phase of this job.
    * @return the current {@link ExecutionPhase}.
    */
   public ExecutionPhase getExecutionPhase() {
      return executionPhase;
   }

   /**
    * Retrieves the creation time of this job.
    * @return the creation time as a {@link ZonedDateTime} instance.
    */
   public ZonedDateTime getCreationTime() {return creationTime; }

   /**
    * Retrieves the start time of this job.
    * @return the start time as a {@link ZonedDateTime} instance.
    */
   public ZonedDateTime getStartTime() {return startTime; }

   /**
    * Retrieves the end time of this job.
    * @return the end time as a {@link ZonedDateTime} instance, or {@code null} if the end time is not set.
    */
   public ZonedDateTime getEndTime() {return endTime; }


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

   /**
    * Submits this job for asynchronous execution using the given executor service.
    * @param executorService the executor service to use for running the job.
    */
    void submitJobToRun(ExecutorService executorService)  {
      jobFuture = CompletableFuture.supplyAsync(getJobCallable(),executorService).thenApply(e ->{logger.info("Job {} completed with phase {}", jobID, e); return e;});//TODO perhaps the API should really deal with the CompletableFuture, with that stored in the JobStore
   }


   /**
    * creates an interface version of the job.
    * @return the UWS {@link org.javastro.ivoa.entities.uws.Job} representation of this job.
    */
   public org.javastro.ivoa.entities.uws.Job asJob() {
      org.javastro.ivoa.entities.uws.Job.Builder<Void> builder = org.javastro.ivoa.entities.uws.Job.builder()
            .withJobId(jobID)
            .withPhase(executionPhase)
            .withCreationTime(creationTime)
            .withParameters(new Parameters(jobSpecification.getParameters().stream()
                  .map(p->{return new Parameter(p.getValue(),false,p.getId(),false);//FIXME parameters can be much more nuanced  - need to think about refactor of org.javastro.ivoacore.uws.description.parameter
                  }).toList()
            ))
            ;
      if (startTime != null) {
         builder.withStartTime(startTime);
      }
      if (endTime != null) {
         builder.withEndTime(endTime);
      }
      if (exception != null) {
         builder.withErrorSummary(new ErrorSummary(exception.getMessage(),ErrorType.FATAL,true));
      }
      if (!results.isEmpty()) {
         builder.withResults(createExternalJobResult());
      }

      return builder.build();
   }

   /**
    * Returns the results of this job as a UWS {@link Results} object.
    * @return the {@link Results} containing references to the job's output parameters.
    */
   public abstract Results createExternalJobResult();

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
    * {@see #getJobFuture()} for a non-blocking way to monitor job completion.
    */
   public void blockingWaitForFinish() {
       // wait for the endTime to be non-null
      while (endTime == null) {
         try {
            Thread.sleep(200); // Sleep for a shortish time to avoid busy waiting
         } catch (InterruptedException e) { // If the thread is interrupted, we should exit the loop and restore the interrupted status - TODO not sure this is the cleanest...
            Thread.currentThread().interrupt(); // Restore the interrupted status
            logger.warn("Thread interrupted while waiting for job {} to finish", jobID);
            break;
         }
      }
   }

   public ShortJobDescription asShortDescription() {

      ShortJobDescription.Builder<Void> builder = ShortJobDescription.builder()
            .withId(jobID)
            .withPhase(executionPhase)
            .withCreationTime(creationTime)
            //.withOwnerId(jobSpecification.getOwnerId()) //FIXME add owner
            .withRunId(jobSpecification.getRunId())
            //.withType("type")//TODO
            //.withHref("href")//TODO
            ;
      return builder.build();
   }
}
