/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


/*
 * Created on 02/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.ivoa.dm.tapschema.Schema;
import org.javastro.ivoa.entities.vosi.tables.Tableset;

import java.util.List;

/**
 * Provides access to the TAP schema definitions for a service.
 */
public interface SchemaProvider {
   /**
    * Return the configured TAP Schemas.
    * @return the list of configured {@link Schema} objects.
    */
   List<Schema> getSchemas();

   /**
    * Return the TAP schema in form suitable for VOSI tables endpoint.
    * @return the {@link Tableset} representation of the TAP schema.
    */
   Tableset asVOSI();
}
