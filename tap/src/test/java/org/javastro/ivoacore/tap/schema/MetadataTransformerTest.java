/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;

import adql.db.DBTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetadataTransformerTest {

   @Test
   void transformToADQLLib() {
       MetadataTransformer mt = new MetadataTransformer(new VODMLSchemaProvider("tapschema.vo-dml.tap.xml", false));
       assertNotNull(mt);
       List<DBTable> adqlmd = mt.transformToADQLLib();
       assertNotNull(adqlmd);
       assertFalse(adqlmd.isEmpty());
       assertEquals(5, adqlmd.size());
       DBTable t1 = adqlmd.get(0);
       assertNotNull(t1);
       assertEquals("SCHEMAS", t1.getADQLName());
       t1.iterator().forEachRemaining(c->{
          assertNotNull(c);
          assertNotNull(c.getADQLName());
          assertNotNull(c.getDatatype());
          assertNotNull(c.getTable());
       });

   }
}