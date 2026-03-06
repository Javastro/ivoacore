/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.*;
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
   private final String jobID;
   protected ExecutionPhase executionPhase;
   protected ZonedDateTime creationTime;
   protected ZonedDateTime startTime;
   protected ZonedDateTime endTime;
   protected final JobSpecification jobSpecification;
   protected List<ParameterValue> results = new ArrayList<>();
   protected CompletableFuture<List<ParameterValue>> jobFuture;


   protected BaseUWSJob(String jobID,JobSpecification jobSpecification) {
      this.jobSpecification = jobSpecification;
      this.jobID = jobID;
      this.executionPhase = ExecutionPhase.PENDING;
      this.creationTime = ZonedDateTime.now(ZoneId.of("UTC"));
   }

   public CompletableFuture<List<ParameterValue>> getJobFuture() {
      return jobFuture;
   }

   public String getID() {
      return jobID;
   }

   public JobSpecification getJobSpecification() {
      return jobSpecification;
   }

   public List<ParameterValue> getResults() {
      return results;
   }

   public ExecutionPhase getExecutionPhase() {
      return executionPhase;
   }


   private Supplier<List<ParameterValue>> getJobCallable() {
      return () -> {
         logger.info("Starting job execution of {}", jobID);
         executionPhase = ExecutionPhase.EXECUTING;
         startTime = ZonedDateTime.now(ZoneId.of("UTC"));
         List<ParameterValue> retval = performAction();
         executionPhase = ExecutionPhase.COMPLETED;
         endTime = ZonedDateTime.now(ZoneId.of("UTC"));
         return retval;
      };
   }

    void submitJobToRun(ExecutorService executorService)  {
      jobFuture = CompletableFuture.supplyAsync(getJobCallable(),executorService).thenApply(p->results=p);//TODO perhaps the API should really deal with the CompletableFuture, with that stored in the JobStore
   }


   /**
    * creates an interface version of the job.
    * @return
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

   public Results getJobResults() {
      //FIXME - this is too simplistic at the moment - need to fetch things properly - need to think about refactor of org.javastro.ivoacore.uws.description.parameter
     Results.Builder<Void> resultsBuilder = Results.builder();

           for (ParameterValue pv : results) {
              resultsBuilder.addResults(ResultReference.builder().withId(pv.getId()).withHref("./results/"+pv.getId()).build()); //IMPL will not work
           }
     return resultsBuilder.build();

   }

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
