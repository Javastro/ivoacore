/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment.parameter;


/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

public class ImmutableStringValue implements ParameterValue {

   private final String val;
   private final String name;

   public ImmutableStringValue(String name,String val) {
      this.val = val;
      this.name = name;
   }

   @Override
   public String getValue() {
      return val;
   }

   @Override
   public boolean isIndirect() {
      return false;
   }

   @Override
   public String getId() {
      return name;
   }
}
