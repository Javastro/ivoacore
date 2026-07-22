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
    /** UTC start time of job execution. */
    public ZonedDateTime startTime;
    /** UTC end time of job execution. */
    public ZonedDateTime endTime;

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
