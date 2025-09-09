/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */ 


package org.javastro.ivoacore.uws.environment.execution;

/**
 * <p>Java class for BinaryEncodings.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
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

    BASE_64("base64"),
    NONE("none");
    private final String value;

    BinaryEncodings(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BinaryEncodings fromValue(String v) {
        for (BinaryEncodings c: BinaryEncodings.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
