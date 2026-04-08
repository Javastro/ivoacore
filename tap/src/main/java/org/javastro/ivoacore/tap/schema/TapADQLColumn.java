/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;


import adql.db.*;
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
   private final DBTable table;
   private final DBType datatype;

   protected TapADQLColumn(TapADQLTable table, Column column) throws NullPointerException {
      super(table.isCaseSensitive()?column.getColumn_name():column.getColumn_name().toUpperCase());//TODO worry about case....
      this.table = table;
      this.column = column;
      this.setCaseSensitive(table.isCaseSensitive());
      this.datatype = MetadataTransformer.mapDbType(column.getDatatype());
      table.addColumn(this);
   }


   private TapADQLColumn(DBTableAlias dbTable, String dbName, String adqlName, Column column) {
      super( adqlName, dbName);
      this.table =  dbTable;
      this.column = column;
      this.setCaseSensitive(dbTable.isCaseSensitive());
      this.datatype = MetadataTransformer.mapDbType(column.getDatatype());
   }
   private TapADQLColumn(TapADQLTable dbTable, String dbName, String adqlName, Column column) {
      super( adqlName, dbName);
      this.table =  dbTable;
      this.column = column;
      this.setCaseSensitive(dbTable.isCaseSensitive());
      this.datatype = MetadataTransformer.mapDbType(column.getDatatype());
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
      log.trace("copy TapADQLColumn new {} {} table={}, orig {} table={}", dbName, adqlName, dbTable.getADQLName(),column.getColumn_name(),table.getADQLName());
      DBColumn retval = null;
      if(dbTable instanceof DBTableAlias) {
       //  retval = new DefaultDBColumn(adqlName, dbName, datatype, dbTable);
        retval =  new TapADQLColumn((DBTableAlias) dbTable,  dbName, adqlName, column);
      }
      else if (dbTable instanceof TapADQLTable) {
         retval =  new TapADQLColumn((TapADQLTable) dbTable,  dbName, adqlName, column);
      }
      else {
         throw new UnsupportedOperationException("Don't know how to copy TapADQLColumn when dbTable is "+dbTable.getClass().getCanonicalName());
      }

      return retval;

   }
}
