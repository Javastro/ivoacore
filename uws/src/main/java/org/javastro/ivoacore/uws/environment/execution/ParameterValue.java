/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.environment.execution;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
//TODO should make this generic to include underlying type

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.javastro.ivoacore.uws.environment.parameter.ImmutableStringValue;

/**
 * Represents a parameter value in a UWS job, with an identifier, a string value, and an indirection flag.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = ImmutableStringValue.class,
                name = "string"
        )
})
public interface ParameterValue {
   /**
    * Returns the string representation of this parameter value.
    * @return the parameter value string.
    */
   String getValue();

   /**
    * Returns whether this parameter value is indirect (i.e., references an external resource).
    * @return {@code true} if the value is a reference to an external resource, {@code false} if it is a direct value.
    */
   boolean isIndirect();

   /**
    * Returns the identifier of this parameter.
    * @return the parameter identifier string.
    */
   String getId();
}
