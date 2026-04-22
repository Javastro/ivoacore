package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.javastro.ivoa.entities.uws.ResultReference;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;
import org.javastro.ivoacore.uws.persist.UWSJobEntity;

import java.util.List;
import java.util.function.Function;

/**
 * A simple UWS job that delegates its execution to a Java {@link java.util.function.Function}.
 */
public class SimpleLambdaJob  extends BaseUWSJob {

   private static final String SIMPLE_LAMBDA = "simpleLambda";
   final Function<String,String> function;

   /**
    * Constructs a SimpleLambdaJob with the given ID, function, and specification.
    * @param jobID the unique identifier for this job.
    * @param func the function that implements the job's action.
    * @param jobSpecification the specification for this job.
    */
   protected SimpleLambdaJob(String jobID, ExecutionEnvironment executionEnvironment, Function<String, String> func, JobSpecification jobSpecification) {
      super(jobID, jobSpecification, executionEnvironment);
      this.function = func;
   }

   @Override
   public Results createExternalJobResult() {
      //FIXME - this is too simplistic at the moment - need to fetch things properly - need to think about refactor of org.javastro.ivoacore.uws.description.parameter
      Results.Builder<Void> resultsBuilder = Results.builder();

      for (ParameterValue pv : results) {
         resultsBuilder.addResults(ResultReference.builder().withId(pv.getId()).withHref("./results/"+pv.getId()).build()); //IMPL will not work
      }
      return resultsBuilder.build();

   }

   @Override
   public List<ParameterValue> performAction() {
      String val = jobSpecification.getParameters().stream().filter(p -> p.getId().equals("input")).findFirst().orElseThrow(IllegalAccessError::new).getValue();
      String res = function.apply(val);

      return List.of(new ParameterValue() {

         @Override
         public String getValue() {
            return res;
         }

         @Override
         public boolean isIndirect() {
            return false;
         }

         @Override
         public String getId() {
            return "result";
         }
      });


   }

   /**
    * Factory for creating {@link SimpleLambdaJob} instances.
    */
   public static class JobFactory extends BaseJobFactory {

      private final Function<String, String> theFunc;

      /**
       * Constructs a JobFactory for SimpleLambdaJob using the given function.
       * @param func the function that the created jobs will execute.
       */
      public JobFactory(Function<String, String> func, EnvironmentFactory environmentFactory) {
         super(SIMPLE_LAMBDA, "a job that runs natively in JVM ", true, environmentFactory);
         this.theFunc = func;
      }



      @Override
      public BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException {
         final String jobID = idProvider.generateId();
         return new SimpleLambdaJob( jobID, environmentFactory.create(jobID), theFunc, jobDescription);
      }

      @Override
      public BaseUWSJob restoreJob(JobSpecification spec, UWSJobEntity entity) {
         SimpleLambdaJob job =
                 new SimpleLambdaJob(
                         entity.jobId,
                         environmentFactory.create(entity.jobId),
                         theFunc,
                         spec
                 );

         job.restoreState(entity.executionPhase, entity.creationTime, entity.startTime, entity.endTime);

         return job;
      }
   }

   /**
    * Job specification for a {@link SimpleLambdaJob}, providing a single string input parameter.
    */
   @JsonTypeName("simpleLambda")
   public static class Specification extends BaseJobSpecification {

      /**
       * Constructs a Specification with the given input value and run ID.
       * @param input the input string to pass to the lambda function.
       * @param runID the run identifier for this job.
       */
      public Specification(final String input, final String runID) {
         super(runID,List.of(new ImmutableStringValue("input", input)));
         this.theParameter = getParameters().get(0);
      }

      @JsonCreator
      public Specification(
              @JsonProperty("runId") String runId,
              @JsonProperty("parameters")
              List<ParameterValue> parameters) {

         super(runId, parameters);
         this.theParameter = parameters.get(0);
      }

      final ParameterValue theParameter;

      @Override
      public String jobTypeIdentifier() {return SIMPLE_LAMBDA;}

      @Override
      public String getJDL() {
         return "";
      }

   }


}
