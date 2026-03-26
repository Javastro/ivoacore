/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


public interface VOTableColumnMetadata {
   String getName();
   String getDescription();
   String getUcd();
   String getUtype();
   String getVOTableDatatype(); //TODO is this needed
   String getUnitString();
}
