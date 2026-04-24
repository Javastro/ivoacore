package org.javastro.ivoacore.uws;

import org.javastro.ivoa.entities.uws.ExecutionPhase;

import java.time.ZonedDateTime;

/**
 * Immutable data carrier representing one persisted UWS job row plus
 * any extra stored metadata required to rehydrate a job.
 * <p>
 * This is persistence-facing, not part of the public API.
 */
public record PersistedJobRecord(
        //This could be removed and just use UWSJobEntity directly, but this is a bit more flexible and decoupled from the persistence layer

        // Core identity
        String jobId,

        // Original submitted job specification values
        JobSpecification specification,

        // Lifecycle state
        ExecutionPhase phase,

        // Timestamps
        ZonedDateTime creationTime,
        ZonedDateTime startTime,
        ZonedDateTime endTime
) {
    public PersistedJobRecord {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId required");
        }
        if (specification == null) {
            throw new IllegalArgumentException("specification required");
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase required");
        }
    }
}