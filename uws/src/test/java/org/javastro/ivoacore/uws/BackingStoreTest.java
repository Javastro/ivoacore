package org.javastro.ivoacore.uws;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.DatabaseJobStore;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mapstruct.factory.Mappers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BackingStoreTest {

    private JobManager jobManager;
    private EntityManagerFactory emf;
    private EntityManager em;
    private DatabaseJobStore store;

    @BeforeAll
    void setup() throws IOException {
        emf = Persistence.createEntityManagerFactory("my-pu");
        em = emf.createEntityManager();

        JobEntityMapper mapper = Mappers.getMapper(JobEntityMapper.class);
        mapper.setObjectMapper(new ObjectMapper());
        store = new DatabaseJobStore(em, mapper);

        File tmpdir = Files.createTempDirectory("managerTest").toFile();
        JobFactoryAggregator agg = new JobFactoryAggregator();
        agg.addFactory(new SimpleLambdaJob.JobFactory(s -> {
            try {
                Thread.sleep(2300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "hello " + s;
        }, new DefaultEnvironmentFactory(tmpdir)));

        MemoryBasedJobStore memoryStore = new MemoryBasedJobStore();
        DefaultExecutionPolicy policy = new DefaultExecutionPolicy();
        jobManager = new JobManager(agg, memoryStore, policy);
    }

    @AfterAll
    void tearDown() {
        if (em != null) {
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    public void testBackingStore() {
        SimpleLambdaJob.Specification spec = new SimpleLambdaJob.Specification("world", "myrefID");
        BaseUWSJob job;
        try {
            job = jobManager.createJob(spec);
        } catch (UWSException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(job);
        store.store(job);

        em.clear();

        List rows = em.createNativeQuery(
                        "SELECT job_id, executionPhase, creationTime FROM uws.uws_jobs WHERE job_id = ?"
                )
                .setParameter(1, job.getID())
                .getResultList();

        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        Object[] row = (Object[]) rows.get(0);
        assertEquals(job.getID(), row[0]);
    }
}
