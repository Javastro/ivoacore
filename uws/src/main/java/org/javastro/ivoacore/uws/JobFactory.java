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
    * Restores a previously persisted UWS job based on the provided job ID, specification,
    * and associated entity. This method is responsible for recreating a {@link BaseUWSJob}
    * instance corresponding to the given parameters.
    * Typically, this method would be used to restore a job from a database or other persistent storage.
    *
    * @param spec the {@link JobSpecification} containing the job's specifications.
    * @param entity the {@link UWSJobEntity} representing the persisted state of the job.
    * @return a {@link BaseUWSJob} instance representing the restored job.
    * @throws UWSException if the job cannot be restored due to an error or invalid parameters.
    */
   BaseUWSJob restoreJob(JobSpecification spec, UWSJobEntity entity) throws UWSException;
}
