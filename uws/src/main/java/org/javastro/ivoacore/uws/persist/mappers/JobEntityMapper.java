package org.javastro.ivoacore.uws.persist.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobSpecification;
import org.javastro.ivoacore.uws.persist.UWSJobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * MapStruct mapper for converting between BaseUWSJob and JobEntity.
 * Handles serialization/deserialization of complex objects using Jackson.
 */
@Mapper(componentModel = "cdi")
public abstract class JobEntityMapper {
    private static final Logger logger = LoggerFactory.getLogger(JobEntityMapper.class);

    @Inject
    ObjectMapper objectMapper;

    @Mapping(target = "jobId", source = "id")
    @Mapping(target = "jobSpecificationJson", expression = "java(serializeSpec(job.getJobSpecification()))")
    @Mapping(target = "exceptionMessage", expression = "java(mapExceptionMessage(job))")
    @Mapping(target = "exceptionType", expression = "java(mapExceptionType(job))")
    public abstract UWSJobEntity toEntity(BaseUWSJob job);

    // --- JSON handling ---

    protected String serializeSpec(JobSpecification spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JobSpecification", e);
        }
    }

    // --- exception helpers ---

    /*protected String mapExceptionMessage(BaseUWSJob job) {
        return job..getException() != null ? job.getException().getMessage() : null;
    }

    protected String mapExceptionType(BaseUWSJob job) {
        return job.getException() != null ? job.getException().getClass().getName() : null;
    }*/
}
