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

/**
 * A UWS Job. This is basically a description of a Job.
 */
public abstract class BaseUWSJob { //IMPL should probably pull some of the methods in this class

   protected static final Logger logger = LoggerFactory.getLogger(BaseUWSJob.class.getName());
   /** The unique identifier for this job. */
   protected final String jobID;
   /** The current execution phase of this job. */
   protected volatile ExecutionPhase executionPhase; //TODO think about thread safety of executionPhase - should it be volatile? should we use an AtomicReference? should we synchronize access to it?
   /** The time at which this job was created. */
   protected ZonedDateTime creationTime;
   /** The time at which this job started executing. */
   protected ZonedDateTime startTime;
   /** The time at which this job finished executing. */
   protected volatile ZonedDateTime endTime;
   /** The specification describing this job's parameters and type. */
   protected final JobSpecification jobSpecification;
   /** The list of result parameter values produced by this job. */
   protected List<ParameterValue> results = new ArrayList<>();

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

      BaseUWSJob(PersistedJobRecord record, ExecutionEnvironment executionEnvironment){
      this.jobID = record.jobId();
      this.jobSpecification = record.specification();
      this.executionPhase = record.phase();
      this.creationTime = record.creationTime();
      this.startTime = record.startTime();
      this.endTime = record.endTime();
      this.executionEnvironment = executionEnvironment;
   }

   /** The execution environment for this job. */
   protected final ExecutionEnvironment executionEnvironment;

   /**
    * Restores the job's state using the data in the provided persisted record.
    * This updates the job's execution phase and timestamps if the job ID in the
    * record matches the current job ID. If there is a mismatch, an exception is thrown.
    *
    * @param record the persisted job record containing the data to restore.
    *               Must have a non-null job ID and execution phase.
    * @throws RuntimeException if the job ID in the record does not match the current job's ID.
    */
   void restore(PersistedJobRecord record) {
      if (record.jobId() != null && jobID.compareToIgnoreCase(record.jobId()) == 0) {
         this.executionPhase = record.phase();
         this.creationTime = record.creationTime();
         this.startTime = record.startTime();
         this.endTime = record.endTime();
      }
       else {
         logger.warn("Attempted to restore job {} with mismatched record ID {}", jobID, record.jobId());
         throw new RuntimeException("Attempted to restore job with mismatched record ID");
      }
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
