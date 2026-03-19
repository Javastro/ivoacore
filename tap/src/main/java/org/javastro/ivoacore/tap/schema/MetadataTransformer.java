/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;

import adql.db.DBTable;
import adql.db.DBType;
import adql.db.DefaultDBColumn;
import adql.db.DefaultDBTable;
import org.ivoa.dm.tapschema.TAPType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to transform metadata to ADQLLib format.
 * @see <a href="https://cdsportal.u-strasbg.fr/adqltuto/gettingstarted.html">ADQLLib Getting Started</a>
 */
public class MetadataTransformer {

   private static final Logger log = LoggerFactory.getLogger(MetadataTransformer.class);
   private final SchemaProvider schemaProvider;

   public MetadataTransformer(SchemaProvider schemaProvider) {
      this.schemaProvider = schemaProvider;
   }

   /**
    * This returns database metadata that can be used by ADQLLib parser.
    *
    * @return
    * @see <a href="https://cdsportal.u-strasbg.fr/adqltuto/gettingstarted.html">ADQLLib Getting Started</a>
    */
   public List<DBTable> transformToADQLLib() {
      List<DBTable> result = new ArrayList<DBTable>();
      for (var s : schemaProvider.getSchemas()) {
         for (var t : s.getTables()) {
            //TODO need to worry about case sensitivity - possibly mapping different ADQL names to DB names
            DefaultDBTable dbTable = new DefaultDBTable(t.getTable_name().toUpperCase(), null);
            dbTable.setADQLSchemaName(s.getSchema_name().toUpperCase());
            dbTable.setDBSchemaName(null);
            for (var c : t.getColumns()) {
               DefaultDBColumn dbColumn = new DefaultDBColumn(c.getColumn_name().toUpperCase(), null, mapDbType(c.getDatatype()), dbTable);
               dbTable.addColumn(dbColumn);
            }
            result.add(dbTable);
         }
      }

      return result;
   }

   private DBType mapDbType(TAPType datatype) {
      //IMPL this is a bit ugly, would be much better to have a mapping table of some sort, but for now this is fine
      //TODO there are some ADQL types that are not in TAPSchema - e.g. circle, interval, etc. - need to decide how to handle these
      DBType result;
      switch (datatype) {
         case VARCHAR:
            result = new DBType(DBType.DBDatatype.VARCHAR);
            break;
         case INTEGER:
            result = new DBType(DBType.DBDatatype.INTEGER);
            break;
         case BIGINT:
            result = new DBType(DBType.DBDatatype.BIGINT);
            break;
         case BOOLEAN:
            result = new DBType(DBType.DBDatatype.SMALLINT); //FIXME really want a boolean type but ADQLLib doesn't support it - use smallint and interpret 0/1 as false/true
            break;
         case SMALLINT:
            result = new DBType(DBType.DBDatatype.SMALLINT);
            break;
         case REAL:
            result = new DBType(DBType.DBDatatype.REAL);
            break;
         case BINARY:
            result = new DBType(DBType.DBDatatype.BINARY);
            break;
         case VARBINARY:
            result = new DBType(DBType.DBDatatype.VARBINARY);
            break;
         case CHAR:
            result = new DBType(DBType.DBDatatype.CHAR);
            break;
         case BLOB:
            result = new DBType(DBType.DBDatatype.BLOB);
            break;
         case CLOB:
            result = new DBType(DBType.DBDatatype.CLOB);
            break;
         case POINT:
            result = new DBType(DBType.DBDatatype.POINT);
            break;
         case REGION:
            result = new DBType(DBType.DBDatatype.REGION);
            break;
         case DOUBLE:
            result = new DBType(DBType.DBDatatype.DOUBLE);
            break;
         case TIMESTAMP:
            result = new DBType(DBType.DBDatatype.TIMESTAMP);
            break;
         default:
            log.error("Unsupported TAPType {} - defaulting to VARCHAR", datatype);
            result = new DBType(DBType.DBDatatype.VARCHAR);
      }
      return result;
   }


}
