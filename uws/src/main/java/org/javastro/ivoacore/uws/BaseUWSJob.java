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
public abstract class BaseUWSJob implements Job {
   private static final Logger logger = LoggerFactory.getLogger(BaseUWSJob.class.getName());
   /** The unique identifier for this job. */
   private final String jobID;
   /** The current execution phase of this job. */
   protected ExecutionPhase executionPhase;
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
   protected CompletableFuture<List<ParameterValue>> jobFuture;


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
   public CompletableFuture<List<ParameterValue>> getJobFuture() {
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


   private Supplier<List<ParameterValue>> getJobCallable() {
      return () -> {
         logger.info("Starting job execution of {}", jobID);
         executionPhase = ExecutionPhase.EXECUTING;
         startTime = ZonedDateTime.now(ZoneId.of("UTC"));
         try {
            List<ParameterValue> retval = performAction();
            executionPhase = ExecutionPhase.COMPLETED;
            endTime = ZonedDateTime.now(ZoneId.of("UTC"));
            return retval;
         }
         catch (Exception e) {
            logger.error("Error during execution of job {}: {}", jobID, e.getMessage(), e);
            executionPhase = ExecutionPhase.ERROR;
            endTime = ZonedDateTime.now(ZoneId.of("UTC"));
            throw new RuntimeException("Error during job execution: " + e.getMessage(), e);
         }

      };
   }

   /**
    * Submits this job for asynchronous execution using the given executor service.
    * @param executorService the executor service to use for running the job.
    */
    void submitJobToRun(ExecutorService executorService)  {
      jobFuture = CompletableFuture.supplyAsync(getJobCallable(),executorService).thenApply(p->results=p);//TODO perhaps the API should really deal with the CompletableFuture, with that stored in the JobStore
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
      switch (executionPhase) {
         case EXECUTING:
            builder.withStartTime(startTime);
            break;
         case COMPLETED:
            builder.withEndTime(startTime);
            builder.withResults(getJobResults());
            break;
      }
      return builder.build();
   }

   /**
    * Returns the results of this job as a UWS {@link Results} object.
    * @return the {@link Results} containing references to the job's output parameters.
    */
   public Results getJobResults() {
      //FIXME - this is too simplistic at the moment - need to fetch things properly - need to think about refactor of org.javastro.ivoacore.uws.description.parameter
     Results.Builder<Void> resultsBuilder = Results.builder();

           for (ParameterValue pv : results) {
              resultsBuilder.addResults(ResultReference.builder().withId(pv.getId()).withHref("./results/"+pv.getId()).build()); //IMPL will not work
           }
     return resultsBuilder.build();

   }

   /**
    * Aborts this job if it is currently running.
    */
   public void abort() {
      boolean cancelled = jobFuture.cancel(true);//TODO need to set the status when this happens
      if (cancelled) {
         logger.info("Job {} aborted", jobID);
         executionPhase = ExecutionPhase.ABORTED;
      }
      else  {
         logger.info("Falied top abort job {}", jobID);
      }
   }
}
