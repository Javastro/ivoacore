package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.description.JobType;
import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.environment.IdProvider;
import org.javastro.ivoacore.uws.environment.UUIDProvider;

/**
 * Base implementation of {@link JobFactory} providing common fields and implementations of {@link org.javastro.ivoacore.uws.description.JobType} methods.
 * @param <T> the type of job to create.
 * @param <S> the class of the job specification.
 */
public abstract class BaseJobFactory<T extends JobType, S extends Class<? extends JobSpecification>> implements JobFactory {

   /** Provider of unique identifiers for new jobs. */
   protected final IdProvider idProvider = new UUIDProvider(); // IMPL fixed for now...
   /**  Factory for creating execution environments for new jobs. */
   protected final EnvironmentFactory environmentFactory;

   /** Job type handled by this factory. */
   protected final T type;

   /** Job specification class handled by this factory. */
   protected final S specClass;

   /**
    * Constructs a new BaseJobFactory.
    * @param type the type of job to create.
    * @param spec the specification for the new job.
    * @param environmentFactory the factory for creating execution environments for new jobs.
    */
   public BaseJobFactory(T type, S spec, EnvironmentFactory  environmentFactory) {

      this.environmentFactory = environmentFactory;
      this.type = type;
      this.specClass = spec;
   }


   /**
    * Get the job Type.
    * @return the JobType
    */
   public T getJobType() {
      return type;
   }

   /**
    * Get the job Specification Class.
    * @return specification class used by this factory.
    */
   public S getSpecificationClass() {
      return specClass;
   }
}
