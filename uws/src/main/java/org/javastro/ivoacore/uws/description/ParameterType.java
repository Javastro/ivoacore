/*
 * Copyright (c) 2025. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.uws.description;


/*
 * Created on 03/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * The type of the parameter.
 * Yet an
 */
public enum ParameterType {
   /** Integer numeric parameter. */
   INTEGER,
   /** Real (floating point) numeric parameter. */
   REAL,
   /** String parameter. */
   STRING,
   /** Complex number parameter. */
   COMPLEX,
   /** Boolean parameter. */
   BOOLEAN,
   /** VOTable parameter. */
   VOTABLE,
   /** Image data parameter. */
   IMAGE,
   /** Spectrum data parameter. */
   SPECTRUM,
   /** FITS file parameter. */
   FITS,
   /** Date/time parameter. */
   DATETIME; // etc...
}
