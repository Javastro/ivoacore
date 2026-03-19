/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment;


public interface EnvironmentFactory {

   ExecutionEnvironment create(String jobId);
}
