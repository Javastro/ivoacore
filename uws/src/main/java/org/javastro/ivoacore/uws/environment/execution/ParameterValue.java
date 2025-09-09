/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment.execution;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
//TODO should make this generic
public interface ParameterValue {
   String getValue();

   boolean isIndirect();

   String getId();
}
