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
    * This returns database metadata that can be used by ADQLLib parser. It includes links to the
    * extra TAPSchema Metadata by using {@link TapADQLTable} and {@link TapADQLColumn}
    * implementations of the ADQLLib {@link DBTable} and {@link adql.db.DBColumn} interfaces.
    *
    * @return
    * @see <a href="https://cdsportal.u-strasbg.fr/adqltuto/gettingstarted.html">ADQLLib Getting Started</a>
    */
   public List<DBTable> transformToADQLLib() {
      List<DBTable> result = new ArrayList<DBTable>();
      for (var s : schemaProvider.getSchemas()) {
         for (var t : s.getTables()) {
            TapADQLTable dbTable = new TapADQLTable(s, t, schemaProvider.isDBCaseSensitive());
            t.getColumns().forEach(c->new TapADQLColumn(dbTable, c));
            result.add(dbTable);
         }
      }

      return result;
   }
   List<DBTable> transformToADQLLibStd() {
      List<DBTable> result = new ArrayList<DBTable>();
      for (var s : schemaProvider.getSchemas()) {
         for (var t : s.getTables()) {
            DefaultDBTable dbTable = new DefaultDBTable(schemaProvider.isDBCaseSensitive() ? t.getTable_name() : t.getTable_name().toUpperCase());
            dbTable.setCaseSensitive(schemaProvider.isDBCaseSensitive());
            t.getColumns().forEach(c->{
               final DefaultDBColumn column = new DefaultDBColumn(schemaProvider.isDBCaseSensitive() ? c.getColumn_name() : c.getColumn_name().toUpperCase(), dbTable);
               column.setCaseSensitive(schemaProvider.isDBCaseSensitive());
               dbTable.addColumn(column);
            });
            result.add(dbTable);
         }
      }
      return result;
   }


   public static  DBType mapDbType(TAPType datatype) {
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


   /**
    * Maps a Java class type (typically from STIL ColumnInfo.getContentClass()) to a TAP TAPType.
    * This is used when creating TAP table metadata from uploaded STIL tables.
    *
    * @param contentClass the Java class representing the column data type (e.g., Double.class, String.class)
    * @return a TAPType corresponding to the Java class, defaulting to VARCHAR if type cannot be determined
    */
    public static TAPType mapContentClassToTAPType(Class<?> contentClass) {
        if (contentClass == null) {
           log.warn("Content class is null, defaulting to VARCHAR");
           return TAPType.VARCHAR;
        }

        // Map Java types to TAPType
        if (String.class.isAssignableFrom(contentClass) || Character.class.isAssignableFrom(contentClass)) {
           return TAPType.VARCHAR;
        }
        if (Integer.class.isAssignableFrom(contentClass) || int.class == contentClass) {
           return TAPType.INTEGER;
        }
        if (Long.class.isAssignableFrom(contentClass) || long.class == contentClass) {
           return TAPType.BIGINT;
        }
        if (Double.class.isAssignableFrom(contentClass) || double.class == contentClass) {
           return TAPType.DOUBLE;
        }
        if (Float.class.isAssignableFrom(contentClass) || float.class == contentClass) {
           return TAPType.REAL;
        }
        if (Boolean.class.isAssignableFrom(contentClass) || boolean.class == contentClass) {
           return TAPType.BOOLEAN;
        }
        if (Short.class.isAssignableFrom(contentClass) || short.class == contentClass) {
           return TAPType.SMALLINT;
        }
        if (byte[].class.isAssignableFrom(contentClass)) {
           return TAPType.VARBINARY;
        }

        // Default to VARCHAR for unknown types
        log.warn("Unknown content class {}, defaulting to VARCHAR", contentClass.getName());
        return TAPType.VARCHAR;
     }

    // New method added to map TAPType -> SQL type string for PostgreSQL
    public static String mapTAPTypeToSqlType(TAPType tapType) {
       if (tapType == null) {
          return "VARCHAR";
       }
       switch (tapType) {
          case VARCHAR:
             return "VARCHAR";
          case CHAR:
             return "CHAR";
          case INTEGER:
             return "INTEGER";
          case BIGINT:
             return "BIGINT";
          case SMALLINT:
             return "SMALLINT";
          case REAL:
             return "REAL";
          case DOUBLE:
             return "DOUBLE PRECISION";
          case BOOLEAN:
             return "BOOLEAN";
          case BINARY:
          case VARBINARY:
          case BLOB:
             return "BYTEA";
          case CLOB:
             return "TEXT";
          case TIMESTAMP:
             return "TIMESTAMP";
          case POINT:
          case REGION:
             return "TEXT"; // spatial types stored as text by default
          default:
             log.warn("Unmapped TAPType {} - defaulting to VARCHAR", tapType);
             return "VARCHAR";
       }
    }
}
