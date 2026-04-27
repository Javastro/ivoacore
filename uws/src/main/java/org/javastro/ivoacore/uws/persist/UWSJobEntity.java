package org.javastro.ivoacore.uws.persist;

import jakarta.persistence.*;
import org.javastro.ivoa.entities.uws.ExecutionPhase;

import java.time.ZonedDateTime;

@Entity
@Table(name = "uws_jobs", schema = "uws")
public class UWSJobEntity {

    @Id
    @Column(name = "job_id")
    public String jobId;

    @Enumerated(EnumType.STRING)
    public ExecutionPhase executionPhase;

    public ZonedDateTime creationTime;
    public ZonedDateTime startTime;
    public ZonedDateTime endTime;

    @Column(name = "job_spec", columnDefinition = "TEXT")
    public String jobSpecificationJson;
}
