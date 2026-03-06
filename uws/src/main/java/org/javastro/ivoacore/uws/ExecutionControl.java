package org.javastro.ivoacore.uws;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Results;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * The internal interface to job control. Many methods are similar to the ones in the external UWS, however they
 * allow access to the internals of jobs.
 */
public interface ExecutionControl  {

   BaseUWSJob createJob(JobSpecification specification) throws UWSException;


   Set<String> listJobIDs() throws UWSException;

   /**
    * Utility method for setting a job into the running phase.
    * @param jobId
    * @throws UWSException
    */
   void runJob(String jobId) throws UWSException;

   /**
    * Utility method for aborting a Job.
    * @param jobId
    * @throws UWSException
    */
   void abortJob(String jobId) throws UWSException;

   /**
    * Set the execution phase of a job. This can be used to start the job running, or abort the job.
    * @param jobId
    * @param newPhase
    * @return the phase that the job has actually been set to.
    * @throws UWSException
    */
   ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException;
   /**
    * set the destruction time for a job. The destruction time is the time at which a job will be deteted from the UWS system.
    * @param jobId
    * @return
    * @throws UWSException
    */
   ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException;

   /**
    * @param jobId
    * @param Long  A length of time that the job can run for.
    * @return
    * @throws UWSException
    */
   Long setExecutionDuration(String jobId, Long Long) throws UWSException;

   /**
    * Delete a job.
    * @param jobId
    * @throws UWSException
    */
   void deleteJob(String jobId) throws UWSException;

   /**
    * return the list of results.
    * @param jobId
    * @return
    * @throws UWSException
    */
   List<ParameterValue> getJobResults(String jobId) throws UWSException;

}
