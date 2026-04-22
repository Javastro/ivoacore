package org.javastro.ivoacore.uws.persist.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobSpecification;
import org.javastro.ivoacore.uws.persist.UWSJobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between BaseUWSJob and JobEntity.
 * Handles serialization/deserialization of complex objects using Jackson.
 */
@Mapper
public abstract class JobEntityMapper {

    protected ObjectMapper objectMapper;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Mapping(target = "jobId", source = "ID")
    @Mapping(target = "jobSpecificationJson", expression = "java(serializeSpec(job.getJobSpecification()))")

    public abstract UWSJobEntity toEntity(BaseUWSJob job);

    /**
     * Serializes a given {@link JobSpecification} instance into its JSON string representation
     * using the configured {@link ObjectMapper}.
     *
     * @param spec the {@link JobSpecification} instance to be serialized
     * @return the JSON string representation of the provided {@link JobSpecification}
     * @throws RuntimeException if serialization fails due to any underlying error
     */
    protected String serializeSpec(JobSpecification spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JobSpecification", e);
        }
    }

    /**
     * Converts a {@link UWSJobEntity} instance into a {@link JobSpecification} by deserializing
     * the JSON string stored in the {@code jobSpecificationJson} field of the provided entity.
     *
     * @param entity the {@link UWSJobEntity} containing the JSON representation of a {@link JobSpecification}
     * @return the deserialized {@link JobSpecification} instance
     * @throws RuntimeException if deserialization fails due to any underlying error
     */
    public JobSpecification toSpecification(UWSJobEntity entity) {
        try {
            return objectMapper.readValue(entity.jobSpecificationJson, JobSpecification.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JobSpecification", e);
        }
    }
}
