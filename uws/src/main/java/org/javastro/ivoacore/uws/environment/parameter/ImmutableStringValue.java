/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment.parameter;


/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

/**
 * An immutable {@link ParameterValue} backed by a fixed string.
 */
@JsonTypeName("string")
public class ImmutableStringValue implements ParameterValue {

   private final String val;
   private final String name;
   private final boolean indirect = false;

   @JsonCreator
   public ImmutableStringValue(
           @JsonProperty("id") String name,
           @JsonProperty("value") String val) {

      this.val = val;
      this.name = name;
   }

   @Override
   public String getValue() {
      return val;
   }

   @Override
   public boolean isIndirect() {
      return indirect;
   }

   @Override
   public String getId() {
      return name;
   }
}
