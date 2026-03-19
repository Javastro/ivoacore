package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.description.JobType;

/**
 * Factory interface for creating UWS jobs of a specific type.
 */
public interface JobFactory extends JobType { //TODO not quite right that JobFactory extends JobType for the JobFactoryAggregator

   /**
    * Creates a new {@link BaseUWSJob} from the given job specification.
    *
    * @param jobDescription the specification describing the job to create.
    * @return the created job.
    * @throws UWSException if the job cannot be created.
    */
   BaseUWSJob createJob(JobSpecification jobDescription) throws UWSException;
}
