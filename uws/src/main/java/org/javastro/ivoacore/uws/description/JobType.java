package org.javastro.ivoacore.uws.description;


/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public interface JobType {

   /**
    * Identifier for the type of the Job.
    * @return
    */
   String jobType();

   /**
    * Human readable description for this type of Job.
    * @return
    */
   String jobDescription();

   /**
    * Whether this class of Job is parameterized, or has a Job description language that is more complex.
    * @return
    */
   boolean isParameterized();
}
