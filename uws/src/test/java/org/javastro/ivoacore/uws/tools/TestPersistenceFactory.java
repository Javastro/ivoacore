package org.javastro.ivoacore.uws.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.javastro.ivoacore.uws.JobFactoryAggregator;
import org.javastro.ivoacore.uws.SimpleLambdaJob;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.mapstruct.factory.Mappers;

public class TestPersistenceFactory {

    public static EntityManagerFactory createEmf() {
        return Persistence.createEntityManagerFactory("my-pu");
    }

    public static DatabaseJobStore createStore(EntityManager em, JobFactoryAggregator agg) {
        JobEntityMapper mapper = Mappers.getMapper(JobEntityMapper.class);
        ObjectMapper om = new ObjectMapper();
        //Subtype needs registering explicitly for jackson/mapstruct to map specialised versions of JobSpecification
        om.registerSubtypes(new NamedType(SimpleLambdaJob.Specification.class, "simpleLambda"));
        //Register other subtypes such as TapJob
        mapper.setObjectMapper(om);
        return new DatabaseJobStore(em, mapper, agg);
    }
}
