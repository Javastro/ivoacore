package org.javastro.ivoacore.uws;


/**
 * Factory interface for creating UWS jobs of a specific type.
 */
public interface JobFactory {
   /**
    * Creates a new {@link BaseUWSJob} from the given job specification.
    *
    * @param jobDescription the specification describing the job to create.
    * @return the created job.
    * @throws UWSException if the job cannot be created.
    */
   RunnableUWSJob createJob(JobSpecification jobDescription) throws UWSException;


}