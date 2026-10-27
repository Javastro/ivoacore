package org.javastro.ivoacore.tap.schema;

import org.ivoa.dm.tapschema.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VODMLSchemaProviderTest {

   @Test
   void provideSchemas() {

      VODMLSchemaProvider prov = new VODMLSchemaProvider("tapschema.vo-dml.tap.xml", false, true);
      assertNotNull(prov);
      List<Schema> schemas = prov.provideSchemas();
      assertEquals(1, schemas.size());
   }
}