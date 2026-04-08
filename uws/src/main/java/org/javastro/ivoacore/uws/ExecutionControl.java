package org.javastro.ivoacore.uws;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * The internal interface to job control. Many methods are similar to the ones in the external UWS, however they
 * allow access to the internals of jobs.
 */
public interface ExecutionControl  {

   /**
    * Creates a new job from the given specification.
    * @param specification the job specification.
    * @return the created {@link BaseUWSJob}.
    * @throws UWSException if the job cannot be created.
    */
   BaseUWSJob createJob(JobSpecification specification) throws UWSException;

   /**
    * Returns the set of IDs for all known jobs.
    * @return the set of job ID strings.
    * @throws UWSException if the job IDs cannot be listed.
    */
   Set<String> listJobIDs() throws UWSException;

   /**
    * Utility method for setting a job into the running phase.
    * @param jobId the identifier of the job to run.
    * @throws UWSException if the job cannot be started.
    */
   void runJob(String jobId) throws UWSException;

   /**
    * Utility method for aborting a Job.
    * @param jobId the identifier of the job to abort.
    * @throws UWSException if the job cannot be aborted.
    */
   void abortJob(String jobId) throws UWSException;

   /**
    * Set the execution phase of a job. This can be used to start the job running, or abort the job.
    * @param jobId the identifier of the job.
    * @param newPhase the new phase string (e.g. "RUN" or "ABORT").
    * @return the phase that the job has actually been set to.
    * @throws UWSException if the phase transition is invalid or fails.
    */
   ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException;
   /**
    * set the destruction time for a job. The destruction time is the time at which a job will be deteted from the UWS system.
    * @param jobId the identifier of the job.
    * @param destructionTime the new destruction time for the job.
    * @return the destruction time that was actually set.
    * @throws UWSException if the destruction time cannot be set.
    */
   ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException;

   /**
    * Set the maximum execution duration for a job.
    * @param jobId the identifier of the job.
    * @param Long A length of time in seconds that the job can run for.
    * @return the execution duration that was actually set.
    * @throws UWSException if the execution duration cannot be set.
    */
   Long setExecutionDuration(String jobId, Long Long) throws UWSException;

   /**
    * Delete a job.
    *
    * @param jobId the identifier of the job to delete.
    * @return
    * @throws UWSException if the job cannot be deleted.
    */
   boolean deleteJob(String jobId) throws UWSException;

   /**
    * return the list of results. This in an internal view.
    * @param jobId the identifier of the job.
    * @return the list of result {@link ParameterValue} objects.
    * @throws UWSException if the results cannot be retrieved.
    */
   List<ParameterValue> getJobResults(String jobId) throws UWSException;


   /**
    * Get any job error detail message.
    * @param jobid the identifier for the Job
    * @return the detail message - or Null is there is no message.
    */
   String jobErrorDetail(String jobid);
}
