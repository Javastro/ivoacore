package org.javastro.ivoacore.uws.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.EntityManager;
import org.javastro.ivoacore.uws.JobFactoryAggregator;
import org.javastro.ivoacore.uws.SimpleLambdaJob;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.mapstruct.factory.Mappers;

/**
 * A factory class responsible for creating instances of {@link DatabaseJobStore}.
 * It ensures that the components required for the persistence layer are
 * properly configured and assembled.
 * <p>
 * This class is designed to be used as a utility and cannot be instantiated.
 */
public final class TestPersistenceFactory {

    private TestPersistenceFactory() {}

    public static DatabaseJobStore create(EntityManager em, JobFactoryAggregator agg) {
        ObjectMapper mapper = objectMapper();

        JobEntityMapper entityMapper = Mappers.getMapper(JobEntityMapper.class);
        entityMapper.setObjectMapper(mapper);

        return new DatabaseJobStore(em, entityMapper, agg);
    }

    /**
     * Creates and configures an {@link ObjectMapper} instance for JSON serialization
     * and deserialization, including registration of custom subtypes.
     *
     * @return a configured {@link ObjectMapper} instance with registered subtypes
     *         for {@link SimpleLambdaJob} and {@link SimpleLambdaJob.Specification}.
     */
    private static ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        // Have to register subtypes so they can be resolved at runtime during deserialisation with Jackson.
        // Only one of the following is actually required, but it depends on whether the concrete class has a
        // @JsonTypeName("<NAME>") annotation or not.
        om.registerSubtypes(new NamedType(SimpleLambdaJob.Specification.class, "SimpleLambdaJob"),
                new NamedType(SimpleLambdaJob.Specification.class, "SimpleLambdaJob$Specification"));

        return om;
    }
}
