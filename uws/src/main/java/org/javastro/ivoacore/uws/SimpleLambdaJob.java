package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;

import java.util.List;
import java.util.function.Function;

public class SimpleLambdaJob  extends BaseUWSJob {

   private static final String SIMPLE_LAMBDA = "simpleLambda";
   final Function<String,String> function;

   protected SimpleLambdaJob(String jobID, Function<String, String> func, JobSpecification jobSpecification) {
      super(jobID, jobSpecification);
      this.function = func;
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

   public static class JobFactory extends BaseJobFactory {

      private final Function<String, String> theFunc;

      public JobFactory(Function<String, String> func) {
         super(SIMPLE_LAMBDA, "a job that runs natively in JVM ", true);
         this.theFunc = func;
      }



      @Override
      public BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException {
         return new SimpleLambdaJob(idProvider.generateId(),  theFunc, jobDescription);
      }
   }

   public static class Specification extends BaseJobSpecification {

      public Specification(final String input, final String runID) {
         super(runID,List.of(new ImmutableStringValue("input", input)));
         this.theParameter = getParameters().get(0);
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
