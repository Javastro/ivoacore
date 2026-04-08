/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


import org.ivoa.dm.tapschema.Column;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.TAPType;
import org.ivoa.dm.tapschema.Table;
import org.junit.jupiter.api.Test;

public class TapADQLMetadataTest {

   @Test
   public void test(){
      TapADQLSchema schema = new TapADQLSchema("test_schema");

      Schema s = new Schema().withSchema_name("test_schema");
      Table t = new Table().withTable_name("test_table");
      TapADQLTable table = new TapADQLTable(s, t, false);
      Column c = new Column().withColumn_name("test_column").withDatatype(TAPType.BIGINT);
      TapADQLColumn column = new TapADQLColumn(table, c);
      System.out.println(column.getName());
      column.getADQLName();
   }
}
