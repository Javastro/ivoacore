

/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.description;

/**
 * The direction of a parameter - whether it is an input to or output from a job.
 */
public enum ParameterDirection {
 /** Input parameter - provided to the job. */
 INPUT,
 /** Output parameter - produced by the job. */
 OUTPUT,
 /** Parameter that is used for both input and output. */
 BOTH;
}



