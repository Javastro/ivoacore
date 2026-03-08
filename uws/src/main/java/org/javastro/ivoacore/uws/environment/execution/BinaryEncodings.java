/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */ 


package org.javastro.ivoacore.uws.environment.execution;

/**
 * <p>Java class for BinaryEncodings.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="BinaryEncodings">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="base64"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 10 Mar 2008
 */

public enum BinaryEncodings {

    /** Base-64 binary encoding. */
    BASE_64("base64"),
    /** No binary encoding; the value is treated as plain text. */
    NONE("none");
    private final String value;

    BinaryEncodings(String v) {
        value = v;
    }

    /**
     * Returns the string representation of this encoding.
     * @return the encoding value string.
     */
    public String value() {
        return value;
    }

    /**
     * Returns the {@code BinaryEncodings} constant corresponding to the given string value.
     * @param v the string value to look up.
     * @return the matching {@code BinaryEncodings} constant.
     * @throws IllegalArgumentException if no matching constant is found.
     */
    public static BinaryEncodings fromValue(String v) {
        for (BinaryEncodings c: BinaryEncodings.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
