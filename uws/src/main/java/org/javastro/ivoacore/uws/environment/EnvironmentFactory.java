/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment;


/**
 * Factory for creating execution environments bound to job identifiers.
 */
public interface EnvironmentFactory {

   /**
    * Creates an execution environment for the supplied job.
    *
    * @param jobId unique job identifier.
    * @return execution environment for that job.
    */
   ExecutionEnvironment create(String jobId);
}
