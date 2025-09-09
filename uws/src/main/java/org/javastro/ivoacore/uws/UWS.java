

package org.javastro.ivoacore.uws;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Results;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;


/**
 * Universal Worker service (UWS) interface operations and external representations.
 */
public interface UWS {
    
    /**
     * List of jobs known to the UWS system.
     *
     * @return
     * @throws UWSException
     */
    Set<String> listJobs() throws UWSException;
    
    /**
     * Get the summary of the current status of a job.
     * @param jobId
     * @return
     * @throws UWSException 
     */
    org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException;
    
    /**
     * Set the execution phase of a job. This can be used to start the job running, or abort the job.
     * @param jobId
     * @param newPhase
     * @return the phase that the job has actually been set to.
     * @throws UWSException 
     */
    ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException;
    /**
     * Get the execution phase of a job.
     * @param jobId
     * @return
     * @throws UWSException 
     */
    ExecutionPhase getPhase(String jobId) throws UWSException;
    
    /**
     * set the destruction time for a job. The destruction time is the time at which a job will be deteted from the UWS system.
     * @param jobId
     * @return
     * @throws UWSException 
     */
    ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException;
    /**
     * @param jobId
     * @param time A length of time that the job can run for.
     * @return
     * @throws UWSException 
     */
    Duration setExecutionDuration(String jobId, Duration time) throws UWSException;
    
    /**
     * Delete a job.
     * @param jobId
     * @throws UWSException 
     */
    void deleteJob(String jobId) throws UWSException;
    
    /**
     * Get the result list for a job. This will not return until the results have been created.
     * @param jobId
     * @return
     * @throws UWSException 
     */
    Results getResults(String jobId) throws UWSException;
    
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
}


