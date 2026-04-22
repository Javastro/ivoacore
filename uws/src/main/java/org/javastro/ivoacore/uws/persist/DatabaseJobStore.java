package org.javastro.ivoacore.uws.persist;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Database-backed implementation of JobStore using JPA.
 * Persists job instances to a relational database.
 */
public class DatabaseJobStore implements JobStore {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseJobStore.class);

    private final EntityManager entityManager;
    private final JobEntityMapper mapper;
    private final JobFactoryAggregator factoryAggregator;

    /**
     * Constructs a DatabaseJobStore with the given EntityManager and mapper.
     *
     * @param entityManager the JPA EntityManager for database operations.
     * @param mapper        the MapStruct mapper for converting between BaseUWSJob and JobEntity.
     */
    public DatabaseJobStore(EntityManager entityManager, JobEntityMapper mapper, JobFactoryAggregator factoryAggregator) {
        this.entityManager = entityManager;
        this.mapper = mapper;
        this.factoryAggregator = factoryAggregator;
    }

    @Override
    public void store(BaseUWSJob job) {
        try {
            UWSJobEntity entity = mapper.toEntity(job);

            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }

            entityManager.merge(entity);
            entityManager.getTransaction().commit();
            logger.debug("Stored/Updated job {} in database", job.getID());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Failed to store job {}", job.getID(), e);
            throw new RuntimeException("Failed to store job", e);
        }
    }

    @Override
    public BaseUWSJob retrieve(String id) {
        UWSJobEntity entity = entityManager.find(UWSJobEntity.class, id);

        if (entity == null) {
            return null;
        }

        JobSpecification spec = mapper.toSpecification(entity);

        BaseUWSJob job;
        try {
            job = factoryAggregator.createJob(id, spec);
            job.restoreState(entity.executionPhase, entity.creationTime, entity.startTime, entity.endTime);
        } catch (UWSException e) {
            throw new RuntimeException(e);
        }

        return job;
    }

    @Override
    public boolean delete(String id) {
        EntityTransaction tx = entityManager.getTransaction();

        try {
            tx.begin();

            UWSJobEntity entity = entityManager.find(UWSJobEntity.class, id);
            if (entity == null) {
                logger.debug("Job {} not found for deletion", id);
                return false;
            }
            entityManager.remove(entity);
            tx.commit();
            logger.debug("Deleted job {} from database", id);
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            logger.error("Failed to delete job {}", id, e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    @Override
    public Set<String> getAllIds() {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT e.jobId FROM UWSJobEntity e",
                    String.class
            );
            List<String> ids = query.getResultList();
            logger.debug("Retrieved {} job IDs from database", ids.size());
            return new HashSet<>(ids);
        } catch (Exception e) {
            logger.error("Failed to retrieve all job IDs", e);
            throw new RuntimeException("Failed to retrieve job IDs", e);
        }
    }

    @Override
    public List<BaseUWSJob> getJobs(ExecutionPhase phase, ZonedDateTime after, Integer last) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT e FROM UWSJobEntity e");
            List<String> conditions = new ArrayList<>();

            if (phase != null) conditions.add("e.executionPhase = :phase");
            if (after != null) conditions.add("e.creationTime > :after");

            if (!conditions.isEmpty()) {
                jpql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            jpql.append(" ORDER BY e.creationTime DESC");

            TypedQuery<UWSJobEntity> query = entityManager.createQuery(jpql.toString(), UWSJobEntity.class);

            if (phase != null) {
                query.setParameter("phase", phase);
            }
            if (after != null) {
                query.setParameter("after", after);
            }
            if (last != null) {
                query.setMaxResults(last);
            }

            List<UWSJobEntity> entities = query.getResultList();
            logger.debug("Retrieved {} jobs matching filters", entities.size());

            // TODO: Convert JobEntity back to BaseUWSJob (requires factory/constructor)
            return List.of();
        } catch (Exception e) {
            logger.error("Failed to retrieve jobs with filters", e);
            throw new RuntimeException("Failed to retrieve jobs", e);
        }
    }
}
