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
     * @return
     * @throws UWSException
     */
    Jobs listJobs(String phase, ZonedDateTime after, Integer last) throws UWSException;

    /**
     * Get the summary of the current status of a job.
     * @param jobId
     * @return
     * @throws UWSException
     */
    org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException;


}
