/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   protected final JobSpecification jobSpecification;
   protected List<ParameterValue> results = new ArrayList<>();
   protected CompletableFuture<List<ParameterValue>> jobFuture;


   protected BaseUWSJob(String jobID,JobSpecification jobSpecification) {
      this.jobSpecification = jobSpecification;
      this.jobID = jobID;
      this.executionPhase = ExecutionPhase.PENDING;
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
         List<ParameterValue> retval = runJob();
         executionPhase = ExecutionPhase.COMPLETED;
         return retval;
      };
   }

    void submitJobToRun(ExecutorService executorService)  {
      jobFuture = CompletableFuture.supplyAsync(getJobCallable(),executorService).thenApply(p->results=p);//TODO perhaps the API should really deal with the CompletableFuture, with that stored in the JobStore
   }


}
