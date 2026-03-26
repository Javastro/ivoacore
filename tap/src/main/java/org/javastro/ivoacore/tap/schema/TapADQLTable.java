/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


import adql.db.DBColumn;
import adql.db.DBIdentifier;
import adql.db.DBTable;
import org.ivoa.dm.tapschema.Schema;
import org.ivoa.dm.tapschema.Table;

import java.util.*;

public class TapADQLTable extends DBIdentifier implements DBTable {


   private final Table table;
   private final List<TapADQLColumn> columns = new ArrayList<>();
   private final Map<String, TapADQLColumn> columnMap = new HashMap<>();
   private final Schema schema;

   public TapADQLTable(Schema s, Table table, boolean dbCaseSensitive) {

      super(dbCaseSensitive?table.getTable_name():table.getTable_name().toUpperCase());//TODO worry about case....
      this.schema = s;
      this.table = table;
      this.adqlCaseSensitive = dbCaseSensitive;
   }


   //FIXME need to do schema...
   @Override
   public String getADQLSchemaName() {
      return this.adqlCaseSensitive?schema.getSchema_name():schema.getSchema_name().toUpperCase();
   }

   @Override
   public String getDBSchemaName() {
      return  this.adqlCaseSensitive?schema.getSchema_name():schema.getSchema_name().toUpperCase();
   }

   @Override
   public String getADQLCatalogName() {
      return "";
   }

   @Override
   public String getDBCatalogName() {
      return null;
   }

   @Override
   public DBColumn getColumn(String colName, boolean adqlName) {
      //TODO need to take account of adqlName
      return columnMap.get(colName);
   }

   @Override
   public DBTable copy(String dbName, String adqlName) {
      // FIXME need to implement
      return null;
   }

   @Override
   public Iterator<DBColumn> iterator() {
      return columns.stream().map(c -> (DBColumn) c).iterator();
   }

   public void addColumn(TapADQLColumn tapADQLColumn) {
      columns.add(tapADQLColumn);
      columnMap.put(tapADQLColumn.getName(), tapADQLColumn);
   }
}
