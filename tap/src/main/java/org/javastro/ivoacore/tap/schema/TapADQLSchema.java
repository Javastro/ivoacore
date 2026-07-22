/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


import adql.db.DBIdentifier;

/**
 * ADQL schema identifier wrapper used by TAP metadata integration.
 */
public class TapADQLSchema extends DBIdentifier {
   /**
    * Creates a TAP ADQL schema identifier.
    *
    * @param adqlName schema name visible to ADQL.
    */
   public TapADQLSchema(String adqlName){
      super(adqlName);
   }
}
