package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.description.JobType;
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

   public static final String JOB_TYPE = "mock";
   public static final String JOB_TYPE_DESCRIPTION = "A mock job that does nothing and is not runnable - just for testing storage";

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


   public static JobType jobType = new JobType() {

      @Override
      public String jobDescription() {
         return JOB_TYPE_DESCRIPTION;
      }

      @Override
      public boolean isParameterized() {
         return true;
      }

      @Override
      public String jobTypeIdentifier() {
         return JOB_TYPE;
      }
   };

   public static class Specification extends BaseJobSpecification {
      public Specification(String runId) {
         super(runId, List.of(new ImmutableStringValue("p1","pval")));
      }

      Specification() {
         super(null, null);
      }

      @Override
      public JobType theJobType() {
         return jobType;
      }
   }

   private final static DefaultEnvironmentFactory envFactory;

   public static class JobFactory extends BaseJobFactory<JobType, Class<Specification>> {
      public JobFactory() {
         super(jobType, Specification.class,envFactory);
      }
      @Override
      public RunnableUWSJob createJob(JobSpecification jobDescription) throws UWSException {
         throw new UWSException("MockJob is not runnable");
      }
   }




      static {
      try {
         envFactory = new DefaultEnvironmentFactory(Files.createTempDirectory("mock").toFile());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }





}
