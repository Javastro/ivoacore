package org.javastro.ivoacore.uws;


import org.javastro.ivoacore.uws.description.JobType;
import org.javastro.ivoacore.uws.persist.UWSJobEntity;

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

   /**
    * Restores a previously persisted UWS job based on the provided UWSJobEntity and specification.
    * Typically, this method would be used to restore a job from a database or other persistent storage.
    *
    * @param jobId  the unique identifier of the job to restore.
    * @param spec   the {@link JobSpecification} containing the job's specifications.
    * @return a {@link BaseUWSJob} instance representing the restored job.
    * @throws UWSException if the job cannot be restored due to an error or invalid parameters.
    */
   //TODO if there's a more generic way to parse the JobSpecification from the UWSJobEntity then this method could be simplified to just take the UWSJobEntity and do the parsing internally.
   BaseUWSJob createJob(String jobId, JobSpecification spec) throws UWSException;
}