/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;

import adql.db.DBChecker;
import adql.db.DBColumn;
import adql.db.DBTable;
import adql.parser.ADQLParser;
import adql.parser.QueryChecker;
import adql.parser.grammar.ParseException;
import adql.query.ADQLSet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MetadataTransformerTest {

   @Test
   void transformToADQLLib() throws ParseException {
       MetadataTransformer mt = new MetadataTransformer(new VODMLSchemaProvider("tapschema.vo-dml.tap.xml", false));
       assertNotNull(mt);
       List<DBTable> tables = mt.transformToADQLLib();
       assertNotNull(tables);
       assertFalse(tables.isEmpty());
       assertEquals(5, tables.size());
       DBTable t1 = tables.get(0);
       assertNotNull(t1);
       assertEquals("SCHEMAS", t1.getADQLName());
       t1.iterator().forEachRemaining(c->{
          assertNotNull(c);
          assertNotNull(c.getADQLName());
          assertNotNull(c.getDatatype());
          assertNotNull(c.getTable());
       });

      QueryChecker checker = new DBChecker(tables);
      ADQLParser parser = new ADQLParser();
      parser.setQueryChecker(checker);
      ADQLSet query = parser.parseQuery("SELECT TOP 8 target_column, target_column_table_name AS taplint_c_2, k.target_column_schema_name, from_column AS taplint_c_4, from_column_table_name, k.from_column_schema_name AS taplint_c_6, key_id, FKCOLUMN_ID AS taplint_c_8  FROM key_columns AS k");
      assertNotNull(query);

      Map<String, ? extends DBColumn> columnMap = Arrays.stream(query.getResultingColumns())
            .collect(Collectors.toMap(
                  col -> col.getADQLName(),
                  col ->  col
            ));
      query = parser.parseQuery("SELECT principal, indexed, std, \"size\" FROM TAP_SCHEMA.columns");
      assertNotNull(query);
   }
   @Test
   void standardMetadataTest() throws ParseException {
      MetadataTransformer mt = new MetadataTransformer(new VODMLSchemaProvider("tapschema.vo-dml.tap.xml", true));
      assertNotNull(mt);
      List<DBTable> tables = mt.transformToADQLLibStd();
      QueryChecker checker = new DBChecker(tables);
      ADQLParser parser = new ADQLParser();
      parser.setQueryChecker(checker);
      ADQLSet query = parser.parseQuery("SELECT TOP 8 target_column, target_column_table_name AS taplint_c_2, k.target_column_schema_name, from_column AS taplint_c_4, from_column_table_name, k.from_column_schema_name AS taplint_c_6, key_id, FKCOLUMN_ID AS taplint_c_8  FROM key_columns AS k");
      assertNotNull(query);

   }
}