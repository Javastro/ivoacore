package org.javastro.ivoacore.uws.persist.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobSpecification;
import org.javastro.ivoacore.uws.SimpleLambdaJob;
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
  //  @Mapping(target = "exceptionMessage", expression = "java(mapExceptionMessage(job))")
  //  @Mapping(target = "exceptionType", expression = "java(mapExceptionType(job))")
    public abstract UWSJobEntity toEntity(BaseUWSJob job);

    protected String serializeSpec(JobSpecification spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JobSpecification", e);
        }
    }

    public JobSpecification toSpecification(UWSJobEntity entity) {
        try {
            return objectMapper.readValue(entity.jobSpecificationJson, SimpleLambdaJob.Specification.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JobSpecification", e);
        }
    }
}
