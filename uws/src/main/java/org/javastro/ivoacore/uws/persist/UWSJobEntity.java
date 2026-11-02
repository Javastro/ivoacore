package org.javastro.ivoacore.uws.persist;

import jakarta.persistence.*;
import org.javastro.ivoa.entities.uws.ExecutionPhase;

import java.io.File;
import java.time.ZonedDateTime;

/**
 * An Entity Class to store Job information.
 */
@Entity
@Table(name = "uws_jobs", schema = "uws")
public class UWSJobEntity {


    /**
     * the job identifier.
     */
    @Id
    @Column(name = "job_id")
    public String jobId;

    /**
     * The execution Phase
     */
    @Enumerated(EnumType.STRING)
    public ExecutionPhase executionPhase;

    /**
     * The time the job was created, started and ended.
     */
    public ZonedDateTime creationTime;
    /** UTC time after which this job record may be destroyed. */
    public ZonedDateTime destructionTime;
    /** Maximum allowed execution duration in seconds. */
    public Integer executionDuration;
    /** UTC start time of job execution. */
    public ZonedDateTime startTime;
    /** UTC end time of job execution. */
    public ZonedDateTime endTime;
    /** Failure message for jobs that ended in the ERROR phase. */
    @Column(name = "error_message", columnDefinition = "text")
    public String errorMessage;

    /**
     * The job specification as a JSON string.
     */
    @Column(name = "job_spec", columnDefinition = "text")
    public String jobSpecificationJson;
    /**
     * The working directory for the job.
     */
    public File workdir;

}
