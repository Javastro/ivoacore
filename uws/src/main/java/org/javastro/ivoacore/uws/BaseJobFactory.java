package org.javastro.ivoacore.uws;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.IdProvider;
import org.javastro.ivoacore.uws.environment.UUIDProvider;

public abstract class BaseJobFactory implements  JobFactory {
   protected final String jobType;
   protected final String jobDescription;
   protected final boolean isParameterized;
   protected final IdProvider idProvider = new UUIDProvider(); // IMPL fixed for now...

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
