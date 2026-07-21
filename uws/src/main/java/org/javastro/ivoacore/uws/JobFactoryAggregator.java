package org.javastro.ivoacore.uws;


/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import java.util.HashMap;
import java.util.Map;

/** A job factory can can create many different types of job.

 */
public class JobFactoryAggregator implements JobFactory {

   private final Map<String, JobFactory> jobFactoryMap=new HashMap<>();

   /**
    * Adds a {@link JobFactory} to this aggregator, registering it by its job type.
    * @param factory the factory to add.
    *
    */
   public void addFactory(String type, JobFactory factory)
   {
      jobFactoryMap.put(type, factory);
   }



   @Override
   public RunnableUWSJob createJob(JobSpecification jobDescription) throws UWSException {
      if(jobFactoryMap.containsKey(jobDescription.jobTypeIdentifier())) {
         return jobFactoryMap.get(jobDescription.jobTypeIdentifier()).createJob(jobDescription);
      }
      else  {
         throw new UWSException("JobType "+jobDescription.jobTypeIdentifier()+" not registered");
      }
   }


}
