package org.javastro.ivoacore.uws.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.mapstruct.factory.Mappers;

public class TestPersistenceFactory {

    public static EntityManagerFactory createEmf() {
        return Persistence.createEntityManagerFactory("my-pu");
    }

    public static DatabaseJobStore createStore(EntityManager em) {
        JobEntityMapper mapper = Mappers.getMapper(JobEntityMapper.class);
        mapper.setObjectMapper(new ObjectMapper());
        return new DatabaseJobStore(em, mapper);
    }
}
