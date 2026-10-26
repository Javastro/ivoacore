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
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A UWS Job. This is basically a description of a Job.
 */
public abstract class BaseUWSJob  { //IMPL should probably pull some of the methods in this class

   /** Logger for job lifecycle events. */
   protected static final Logger logger = LoggerFactory.getLogger(BaseUWSJob.class.getName());
   /** The unique identifier for this job. */
   protected final String jobID;
   /** The current execution phase of this job. */
   protected volatile ExecutionPhase executionPhase; //TODO think about thread safety of executionPhase - should it be volatile? should we use an AtomicReference? should we synchronize access to it?
   /** The time at which this job was created. */
   protected ZonedDateTime creationTime;
   /** The time at which this job should be destroyed. */
   protected ZonedDateTime destructionTime;
   /** The maximum execution duration for this job, in seconds. */
   protected int executionDuration;
   /** The time at which this job started executing. */
   protected ZonedDateTime startTime;
   /** The time at which this job finished executing. */
   protected volatile ZonedDateTime endTime;
   /** The specification describing this job's parameters and type. */
   protected final JobSpecification jobSpecification;
   /** The execution environment for this job. */
   protected final ExecutionEnvironment executionEnvironment;
   /** The execution policy for this job. */
   protected final ExecutionPolicy executionPolicy;
   /** The list of result parameter values produced by this job. */
   protected List<ParameterValue> results = new ArrayList<>();

   /** Any exception that occurred during the execution of this job. */
   protected UWSException exception;


   /**
    * Constructs a new BaseUWSJob with the given job ID and specification.
    * @param jobID the unique identifier for this job.
    * @param jobSpecification the specification describing the job's parameters and type.
    * @param executionEnvironment execution context for filesystem and security resources.
    * @param executionPolicy execution policy governing job execution.
    */
   protected BaseUWSJob(String jobID, JobSpecification jobSpecification, ExecutionEnvironment executionEnvironment, ExecutionPolicy executionPolicy) {
      this.jobSpecification = jobSpecification;
      this.jobID = jobID;
      this.executionEnvironment = executionEnvironment;
      this.executionPolicy = executionPolicy;
      this.executionPhase = ExecutionPhase.PENDING;
      this.creationTime = ZonedDateTime.now(ZoneId.of("UTC"));
      this.destructionTime = creationTime.plusSeconds(executionPolicy.getDefaultLifetime());
      this.executionDuration = executionPolicy.getMaxRunTime();
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

   /**
    * Retrieves the destruction time of this job.
    * @return the destruction time as a {@link ZonedDateTime} instance.
    */
   public ZonedDateTime getDestructionTime() { return destructionTime; }

   /**
    * Sets the destruction time of this job.
    * @param destructionTime the new destruction time.
    */
   public void setDestructionTime(ZonedDateTime destructionTime) { this.destructionTime = destructionTime; }

   /**
    * Retrieves the maximum execution duration for this job, in seconds.
    * @return the execution duration.
    */
   public int getExecutionDuration() { return executionDuration; }

   /**
    * Sets the maximum execution duration for this job, in seconds.
    * @param executionDuration the new execution duration.
    */
   public void setExecutionDuration(int executionDuration) { this.executionDuration = executionDuration; }

   /**
    * Retrieves the execution policy associated with this job.
    * The execution policy defines constraints and behaviours, such as
    * maximum runtime, concurrency limits, and lifecycle management,
    * that govern the job's execution within the system.
    *
    * @return the {@link ExecutionPolicy} instance associated with this job.
    */
   public ExecutionPolicy getExecutionPolicy() {return executionPolicy; }


   /**
    * creates an interface version of the job.
    * @return the UWS {@link org.javastro.ivoa.entities.uws.Job} representation of this job.
    */
   public org.javastro.ivoa.entities.uws.Job asJob() {
      org.javastro.ivoa.entities.uws.Job.Builder<Void> builder = org.javastro.ivoa.entities.uws.Job.builder()
            .withJobId(jobID)
            .withPhase(executionPhase)
            .withCreationTime(creationTime)
            .withDestruction(destructionTime)
            .withExecutionDuration(executionDuration)
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
    * Creates a compact job description suitable for job list responses.
    *
    * @return short description of this job.
    */
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
