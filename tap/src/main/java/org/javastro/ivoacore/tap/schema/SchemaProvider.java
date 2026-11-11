/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;

import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.TAPSchemaUtils;
import org.javastro.ivoa.entities.vosi.tables.Tableset;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to the TAP schema definitions for a service.
 */
public interface SchemaProvider {
   /**
    * Return the configured TAP Schemas. These schemas are fully normalised.
    * @return the list of configured {@link Schema} objects.
    */
   List<Schema> getSchemas();

   /**
    * Return the configured TAP Schemas. These schemas have the table names denormalised to have the schema included as required by the TAP standard.
    * @return the list of configured {@link Schema} objects.
    */
   default List<Schema> getSchemasForDB() {
      List<Schema> retval = new ArrayList<>();
      for (Schema s : getSchemas()) {
         retval.add(TAPSchemaUtils.adjustforTAPStd(s));
      }
      return retval;
   }
   /**
    * Return the TAP schema in form suitable for VOSI tables endpoint.
    * @return the {@link Tableset} representation of the TAP schema.
    */
   Tableset asVOSI();

   /**
    * Is the database case sensitive. This is needed by ADQLLib to determine how to handle identifiers.
    * @return {@code true} when identifiers are case-sensitive.
    */
   boolean isDBCaseSensitive();
}
