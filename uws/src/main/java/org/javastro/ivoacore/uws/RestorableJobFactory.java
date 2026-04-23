package org.javastro.ivoacore.uws;

import org.javastro.ivoacore.uws.description.JobType;

/**
 * Internal-only factory contract for restoring jobs from persistence.
 * Not part of the public API.
 */
interface RestorableJobFactory extends JobType {
    /**
     * Restores a previously persisted UWS job.
     *
     * @param jobId the persisted job identifier
     * @param spec the restored job specification
     * @return the restored job instance
     * @throws UWSException if restoration fails
     */
    BaseUWSJob createJob(String jobId, JobSpecification spec) throws UWSException;
}
