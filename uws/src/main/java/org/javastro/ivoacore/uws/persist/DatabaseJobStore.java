package org.javastro.ivoacore.uws.persist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.persist.mappers.JobEntityMapper;
import org.mapstruct.factory.Mappers;
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
 *
 * Calls to store and delete are transactional, they must be invoked within an
 * active transaction when called.
 */
public class DatabaseJobStore implements JobStore {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseJobStore.class);

    private final EntityManager entityManager;
    private final JobEntityMapper mapper;
    /**
     * Constructs a DatabaseJobStore with the given EntityManager and mapper.
     *
     * @param entityManager the JPA EntityManager for database operations.
     * @param objectMapper the ObjectMapper used for JSON serialization/deserialization of job specifications.
     * @param factoryAggregator the JobFactoryAggregator used to create job instances.
     */
    public DatabaseJobStore(EntityManager entityManager, ObjectMapper objectMapper, JobFactoryAggregator factoryAggregator) {
        this.entityManager = entityManager;
        this.mapper = Mappers.getMapper(JobEntityMapper.class);
        this.mapper.setObjectMapper(objectMapper);
    }

    /**
     * Constructs a DatabaseJobStore with the given EntityManager, type details, and JobFactoryAggregator.
     *
     * @param entityManager the JPA EntityManager used for database operations.
     * @param typeDetails the NamedType specifying the subtypes for JSON deserialization.
     * @param factoryAggregator the JobFactoryAggregator used to create job instances.
     */
    public DatabaseJobStore(EntityManager entityManager, NamedType typeDetails, JobFactoryAggregator factoryAggregator) {
        this(entityManager, objectMapperFor(typeDetails), factoryAggregator);
    }

    public static DatabaseJobStore forJobType(EntityManager entityManager, Class<? extends JobSpecification> specificationClass,
            String typeName, JobFactoryAggregator factoryAggregator) {

        return new DatabaseJobStore(entityManager, new NamedType(specificationClass, typeName), factoryAggregator);
    }

    /**
     * Creates and returns a configured {@link ObjectMapper} instance for handling serialization and
     * deserialization of JSON, specifically registering subtypes as specified by the given type details.
     *
     * @param typeDetails the {@link NamedType} representing the subtypes to register for JSON handling.
     * @return a configured {@link ObjectMapper} instance with the registered subtypes.
     */
    private static ObjectMapper objectMapperFor(NamedType typeDetails) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(typeDetails);
        return objectMapper;
    }

    /**
     * Persists a BaseUWSJob instance into the database by converting it into an appropriate
     * entity representation and delegating to the EntityManager for persistence.
     * If the operation fails, logs the error and rethrows a RuntimeException.
     * <p>
     * Must be invoked within an active transaction when required by the persistence provider.
     * @param job the BaseUWSJob instance to be stored in the database.
     * @throws RuntimeException if the job cannot be stored due to an underlying issue.
     */
    @Override
    public void store(BaseUWSJob job) {
        try {
            UWSJobEntity entity = mapper.toEntity(job);
            entityManager.merge(entity);

            logger.debug("Stored/Updated job {} in database", job.getID());
        } catch (Exception e) {
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

        BaseUWSJob job;
        try {
            job = reinstateJob(entity);
        } catch (UWSException e) {
            throw new RuntimeException(e);
        }

        return job;
    }

    /**
     * Deletes a job from the persistent store based on the provided job identifier.
     * If the job does not exist, no action will be performed.
     * <p>
     * Must be invoked within an active transaction when required by the persistence provider.
     * @param id the unique identifier of the job to be deleted.
     * @return {@code true} if the job was successfully deleted, {@code false} if no job with the specified identifier exists.
     * @throws RuntimeException if an error occurs during the deletion process.
     */
    @Override
    public boolean delete(String id) {
        try {
            UWSJobEntity entity = entityManager.find(UWSJobEntity.class, id);
            if (entity == null) {
                logger.debug("Job {} not found for deletion", id);
                return false;
            }
            entityManager.remove(entity);
            logger.debug("Deleted job {} from database", id);
            return true;
        } catch (Exception e) {
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

            jpql.append(" ORDER BY e.creationTime ASC");

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

            // Convert JobEntity back to BaseUWSJob
            List<BaseUWSJob> jobs = new ArrayList<>();
            for (UWSJobEntity entity : entities) {
                try {
                    BaseUWSJob job = reinstateJob(entity);
                    jobs.add(job);
                } catch (UWSException e) {
                    logger.error("Failed to reinstate job {}", entity.jobId, e);
                }
            }
            return jobs;
        } catch (Exception e) {
            logger.error("Failed to retrieve jobs with filters", e);
            throw new RuntimeException("Failed to retrieve jobs", e);
        }
    }

    /**
     * Reinstates a job from a given UWSJobEntity, restoring its state and metadata to create a
     * functional BaseUWSJob instance.
     *
     * @param entity the UWSJobEntity instance containing the job's persisted state and metadata.
     * @return the restored BaseUWSJob instance.
     * @throws UWSException if an error occurs while creating or restoring the job's state.
     */
    private BaseUWSJob reinstateJob(UWSJobEntity entity) throws UWSException {
        JobSpecification spec = mapper.toSpecification(entity);
        return new RestoredUWSJob(entity,spec);
    }
}
