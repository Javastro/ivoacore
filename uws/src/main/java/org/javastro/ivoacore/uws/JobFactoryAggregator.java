package org.javastro.ivoacore.uws;


/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.persist.UWSJobEntity;

import java.util.HashMap;
import java.util.Map;

/** A job factory can can create many different types of job.

 */
public class JobFactoryAggregator implements JobFactory, RestorableJobFactory {

   private final Map<String, CommonJobFactory> jobFactoryMap=new HashMap<>();

   /**
    * Adds a {@link JobFactory} to this aggregator, registering it by its job type.
    * @param factory the factory to add.
    */
   public void addFactory(CommonJobFactory factory)
   {
      jobFactoryMap.put(factory.jobType(), factory);
   }



   @Override
   public BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException {
      if(jobFactoryMap.containsKey(jobDescription.jobTypeIdentifier())) {
         return jobFactoryMap.get(jobDescription.jobTypeIdentifier()).createJob(jobDescription);
      }
      else  {
         throw new UWSException("JobType "+jobDescription.jobTypeIdentifier()+" not registered");
      }
   }

   @Override
   public BaseUWSJob createJob(String jobId, JobSpecification spec) throws UWSException {
      RestorableJobFactory factory = jobFactoryMap.get(spec.jobTypeIdentifier());

      if (factory == null) {
         throw new RuntimeException("No factory for job type " + spec.jobTypeIdentifier());
      }

      return factory.createJob(jobId, spec);
   }

   @Override
   public String jobType() {
      return "JobFactoryAggregator";
   }

   @Override
   public String jobDescription() {
      return "A JobFactory that aggregates several types of JobFactory";
   }


   @Override
   public boolean isParameterized() {
      return false; //IMPL This does not really have much meaning for this aggregator....
   }
}
