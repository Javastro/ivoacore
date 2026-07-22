package org.javastro.ivoacore.uws;


/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.javastro.ivoacore.uws.description.JobType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A job factory can can create many different types of job.

 */
public class JobFactoryAggregator implements JobFactory {

   private final Map<String, BaseJobFactory<? extends JobType, ? extends Class<? extends JobSpecification>>> jobFactoryMap=new HashMap<>();

   /**
    * Adds a {@link JobFactory} to this aggregator, registering it by its job type.
    * @param factory the factory to add.
    *
    */
   public <T extends JobType, S extends Class<? extends JobSpecification>> void addFactory(BaseJobFactory<T, S> factory)
   {
      jobFactoryMap.put(factory.getJobType().jobTypeIdentifier(), factory);
   }



   @Override
   public RunnableUWSJob createJob(JobSpecification jobDescription) throws UWSException {
      if(jobFactoryMap.containsKey(jobDescription.theJobType().jobTypeIdentifier())) {
         return jobFactoryMap.get(jobDescription.theJobType().jobTypeIdentifier()).createJob(jobDescription);
      }
      else  {
         throw new UWSException("JobType "+jobDescription.theJobType().jobTypeIdentifier()+" not registered");
      }
   }

   /**
    *
    * @return the list of named type mappings for the specifid
    */
   public List<NamedType> getSpecificationMapping() {
      return jobFactoryMap.values().stream().map(f->new NamedType(f.getSpecificationClass(),f.getJobType().jobTypeIdentifier())).toList();
   }


}
