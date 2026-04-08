/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


import adql.db.DBColumn;
import adql.db.DBIdentifier;
import adql.db.DBTable;
import adql.db.DBType;
import org.ivoa.dm.tapschema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link DBColumn} and {@link VOTableColumnMetadata} for a TAP column, based on the TAPSchema column metadata.
 * This class is immutable.
 */
public class TapADQLColumn extends DBIdentifier implements DBColumn, VOTableColumnMetadata {
   private static final Logger log = LoggerFactory.getLogger(TapADQLColumn.class);
   private final Column column;
   private final TapADQLTable table;
   private final DBType datatype;

   protected TapADQLColumn(TapADQLTable table, Column column) throws NullPointerException {
      super(table.isCaseSensitive()?column.getColumn_name():column.getColumn_name().toUpperCase());//TODO worry about case....
      this.table = table;
      this.column = column;
      this.setCaseSensitive(table.isCaseSensitive());
      this.datatype = MetadataTransformer.mapDbType(column.getDatatype());
      table.addColumn(this);
   }

   @Override
   public String getName() {
      return column.getColumn_name();
   }

   @Override
   public String getDescription() {
      return column.getDescription();
   }

   @Override
   public String getUcd() {
      return column.getUcd();
   }

   @Override
   public String getUtype() {
      return column.getUtype();
   }

   @Override
   public String getVOTableDatatype() {
      log.error("getVOTableDatatype not implemented for TapADQLColumn");
      return "";
   }

   @Override
   public String getUnitString() {
      //FIXME get units.
      return "";
   }

   @Override
   public DBType getDatatype() {
      return datatype;
   }

   @Override
   public DBTable getTable() {
      return table;
   }

   @Override
   public DBColumn copy(String dbName, String adqlName, DBTable dbTable) {
      log.info("copy TapADQLColumn new {} {} table={}, orig {} table={}", dbName, adqlName, dbTable.getADQLName(),column.getColumn_name(),table.getADQLName());
      //TODO what to do when dbName is allowed to be different.
      return new TapADQLColumn(dbTable instanceof TapADQLTable ? (TapADQLTable) dbTable : table, column); //FIXME is this OK - not really dealt with new name etc - need to understand the purpose of the copy
   }
}
