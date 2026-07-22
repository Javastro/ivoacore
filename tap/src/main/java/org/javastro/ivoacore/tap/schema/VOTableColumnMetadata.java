/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


/**
 * Additional VOTable metadata exposed for ADQL columns.
 */
public interface VOTableColumnMetadata {
   /** @return logical column name. */
   String getName();
   /** @return human-readable column description. */
   String getDescription();
   /** @return UCD value, if present. */
   String getUcd();
   /** @return UType value, if present. */
   String getUtype();
   /** @return VOTable datatype token. */
   String getVOTableDatatype(); //TODO is this needed
   /** @return column units string, if available. */
   String getUnitString();
}
