package org.javastro.ivoacore.uws;


/*
 * Created on 06/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoa.entities.uws.Jobs;

import java.time.ZonedDateTime;

/**
 * Core UWS functionality. Either getting some base objects, or setting state.
 *  Note that there are no JAX-WS-RS annotations on this interface as it is shared with the actual implementation, not just the web
 *  presentation.
 */
public interface UWSCore {

    /**
     * List of jobs known to the UWS system.
     *
     * @param phase filter jobs by execution phase; may be {@code null} for no filtering.
     * @param after filter jobs created after this time; may be {@code null} for no filtering.
     * @param last return only the last N jobs; may be {@code null} for no limit.
     * @return the list of jobs matching the filter criteria.
     * @throws UWSException if there is an error accessing the job list.
     */
    Jobs listJobs(String phase, ZonedDateTime after, Integer last) throws UWSException;

    /**
     * Get the summary of the current status of a job.
     * @param jobId the identifier of the job to retrieve.
     * @return the UWS {@link org.javastro.ivoa.entities.uws.Job} representing the current state of the job.
     * @throws UWSException if the job cannot be found or accessed.
     */
    org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException;


}
