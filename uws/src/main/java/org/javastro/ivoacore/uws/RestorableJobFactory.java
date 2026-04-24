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
     * @param record the restored job's details from persistence
     * @return the restored job instance
     * @throws UWSException if restoration fails
     */
    BaseUWSJob createJob(String jobId, PersistedJobRecord record) throws UWSException;
}
