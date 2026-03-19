package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.EnvironmentFactory;
import org.javastro.ivoacore.uws.environment.IdProvider;
import org.javastro.ivoacore.uws.environment.UUIDProvider;

/**
 * Base implementation of {@link JobFactory} providing common fields and implementations of {@link org.javastro.ivoacore.uws.description.JobType} methods.
 */
public abstract class BaseJobFactory implements  JobFactory {
   /** The identifier for the type of job this factory creates. */
   protected final String jobType;
   /** A human-readable description of the job type. */
   protected final String jobTypeDescription;
   /** Whether jobs created by this factory are parameterized. */
   protected final boolean isParameterized;
   /** Provider of unique identifiers for new jobs. */
   protected final IdProvider idProvider = new UUIDProvider(); // IMPL fixed for now...
   /**  Factory for creating execution environments for new jobs. */
   protected final EnvironmentFactory environmentFactory;
   /**
    * Constructs a new BaseJobFactory.
    * @param jobType the identifier for the job type.
    * @param jobTypeDescription a human-readable description of the job type.
    * @param isParameterized {@code true} if jobs are parameterized, {@code false} otherwise.
    */
   public BaseJobFactory(String jobType, String jobTypeDescription, boolean isParameterized, EnvironmentFactory  environmentFactory) {
      this.jobType = jobType;
      this.jobTypeDescription = jobTypeDescription;
      this.isParameterized = isParameterized;
      this.environmentFactory = environmentFactory;
   }

   @Override
   public String jobType() {
      return jobType;
   }

   @Override
   public String jobDescription() {
      return jobTypeDescription;
   }

   @Override
   public boolean isParameterized() {
      return isParameterized;
   }
}
