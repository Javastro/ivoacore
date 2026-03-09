package org.javastro.ivoacore.uws.description;


/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * Describes a type of UWS job, providing its identifier, description, and whether it is parameterized.
 */
public interface JobType {

   /**
    * Identifier for the type of the Job.
    * @return the job type identifier string.
    */
   String jobType();

   /**
    * Human readable description for this type of Job.
    * @return a human-readable description of the job type.
    */
   String jobDescription();

   /**
    * Whether this class of Job is parameterized, or has a Job description language that is more complex.
    * @return {@code true} if the job is parameterized, {@code false} otherwise.
    */
   boolean isParameterized();
}
