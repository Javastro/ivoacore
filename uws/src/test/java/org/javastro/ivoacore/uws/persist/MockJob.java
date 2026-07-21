package org.javastro.ivoacore.uws.persist;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.BaseJobSpecification;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Mock implementation of a UWS job for testing purposes. This class extends {@link BaseUWSJob} and provides a simple job specification with a single parameter.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public class MockJob extends BaseUWSJob {

   /**
    * Constructs a new BaseUWSJob with the given job ID and specification.
    *
    * @param jobID     the unique identifier for this job.
    * @param cDate
    * @param phase
    */
   protected MockJob(String jobID, String runid, ZonedDateTime cDate, ExecutionPhase phase) {
      super(jobID, new Specification(runid), envFactory.create(jobID));
      this.creationTime = cDate;
      this.executionPhase = phase;
   }

   @Override
   public Results createExternalJobResult() {
      return null;
   }

   public void setExecutionPhase(ExecutionPhase executionPhase) {
      this.executionPhase = executionPhase;
   }


   public static class Specification extends BaseJobSpecification {
      public Specification(String runId) {
         super(runId, List.of(new ImmutableStringValue("p1","pval")));
      }

      Specification() {
         super(null, null);
      }

      @Override
      public String jobDescription() {
         return "A mock job that does nothing and is not runnable - just for testing storage";
      }

      @Override
      @JsonIgnore
      public boolean isParameterized() {
         return false;
      }

      @Override
      public String jobTypeIdentifier() {
         return "mock";
      }

   }

   private final  static DefaultEnvironmentFactory envFactory;

   static {
      try {
         envFactory = new DefaultEnvironmentFactory(Files.createTempDirectory("mock").toFile());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }



}
