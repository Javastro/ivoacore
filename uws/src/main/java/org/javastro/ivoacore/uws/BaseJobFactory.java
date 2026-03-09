package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.IdProvider;
import org.javastro.ivoacore.uws.environment.UUIDProvider;

/**
 * Base implementation of {@link JobFactory} providing common fields and implementations of {@link org.javastro.ivoacore.uws.description.JobType} methods.
 */
public abstract class BaseJobFactory implements  JobFactory {
   /** The identifier for the type of job this factory creates. */
   protected final String jobType;
   /** A human-readable description of the job type. */
   protected final String jobDescription;
   /** Whether jobs created by this factory are parameterized. */
   protected final boolean isParameterized;
   /** Provider of unique identifiers for new jobs. */
   protected final IdProvider idProvider = new UUIDProvider(); // IMPL fixed for now...

   /**
    * Constructs a new BaseJobFactory.
    * @param jobType the identifier for the job type.
    * @param jobDescription a human-readable description of the job type.
    * @param isParameterized {@code true} if jobs are parameterized, {@code false} otherwise.
    */
   public BaseJobFactory(String jobType, String jobDescription, boolean isParameterized) {
      this.jobType = jobType;
      this.jobDescription = jobDescription;
      this.isParameterized = isParameterized;
   }

   @Override
   public String jobType() {
      return jobType;
   }

   @Override
   public String jobDescription() {
      return jobDescription;
   }

   @Override
   public boolean isParameterized() {
      return isParameterized;
   }
}
