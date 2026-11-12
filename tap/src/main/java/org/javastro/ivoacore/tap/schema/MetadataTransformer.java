/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.tap.schema;

import adql.db.DBTable;
import adql.db.DBType;
import adql.db.DefaultDBColumn;
import adql.db.DefaultDBTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.starlink.table.ColumnInfo;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

/**
 * Utility class to transform metadata to ADQLLib format.
 * @see <a href="https://cdsportal.u-strasbg.fr/adqltuto/gettingstarted.html">ADQLLib Getting Started</a>
 */
public class MetadataTransformer {

   private static final Logger log = LoggerFactory.getLogger(MetadataTransformer.class);
   private final SchemaProvider schemaProvider;

   /**
    * Creates a metadata transformer for a schema provider.
    *
    * @param schemaProvider schema source used for transformation.
    */
   public MetadataTransformer(SchemaProvider schemaProvider) {
      this.schemaProvider = schemaProvider;
   }

   /**
    * This returns database metadata that can be used by ADQLLib parser. It includes links to the
    * extra TAPSchema Metadata by using {@link TapADQLTable} and {@link TapADQLColumn}
    * implementations of the ADQLLib {@link DBTable} and {@link adql.db.DBColumn} interfaces.
    *
    * @return ADQLLib table metadata derived from TAP_SCHEMA content.
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

   //---------------------------------------- Map from class to dbType -----------------------------
   private record TypeMapping(
           Predicate<Class<?>> matcher,
           String tapType) {
   }

   private static final List<TypeMapping> CLASS_TO_TAP = List.of(
           new TypeMapping(Objects::isNull, "VARCHAR"),
           new TypeMapping(c -> String.class.isAssignableFrom(c) || Character.class.isAssignableFrom(c), "VARCHAR"),
           new TypeMapping(c -> Integer.class.isAssignableFrom(c) || c == int.class, "INTEGER"),
           new TypeMapping(c -> Long.class.isAssignableFrom(c) || c == long.class, "BIGINT"),
           new TypeMapping(c -> Double.class.isAssignableFrom(c) || c == double.class, "DOUBLE"),
           new TypeMapping(c -> Float.class.isAssignableFrom(c) || c == float.class, "REAL"),
           new TypeMapping(c -> Boolean.class.isAssignableFrom(c) || c == boolean.class, "BOOLEAN"),
           new TypeMapping(c -> Short.class.isAssignableFrom(c) || c == short.class, "SMALLINT"),
           new TypeMapping(byte[].class::isAssignableFrom, "VARBINARY"), new TypeMapping(BigDecimal.class::isAssignableFrom, "DOUBLE"),
           new TypeMapping(c -> java.sql.Timestamp.class.isAssignableFrom(c) || java.util.Date.class.isAssignableFrom(c), "TIMESTAMP")
   );

   /**
    * Maps a ColumnInfo to a TAPType, using the content class and optionally the xtype.
    * @param columnInfo The ColumnInfo derived from a VOTable.
    * @return the TAP Type.
    */
   public static String mapVOTableToTAPType(ColumnInfo  columnInfo) {
      String retval = mapContentClassToTAPType(columnInfo != null ? columnInfo.getContentClass() : null);
      if(columnInfo.getXtype() != null) {
         switch (columnInfo.getXtype().toUpperCase()) {
            case "TIMESTAMP" -> retval = "TIMESTAMP";
            case "POINT" -> retval = "POINT";
            case "REGION" -> retval = "REGION";
            case "POLYGON" -> retval = "POLYGON"; //FIXME these below are not "standard"
            case "CIRCLE" -> retval = "CIRCLE";
            case "ELLIPSE" -> retval = "ELLIPSE";
         }
      }
      return retval;
   }

   /**
    * Maps a Java content class to a TAP_SCHEMA datatype.
    *
    * @param contentClass Java class to map.
    * @return matching TAP datatype, defaulting to VARCHAR.
    *
    */
   public static String mapContentClassToTAPType(Class<?> contentClass) {
      return CLASS_TO_TAP.stream()
              .filter(m -> m.matcher().test(contentClass))
              .map(TypeMapping::tapType)
              .findFirst()
              .orElseGet(() -> {
                 log.warn("Unknown content class {}, defaulting to VARCHAR",
                         contentClass != null ? contentClass.getName() : "null");
                 return "VARCHAR";
              });
   }

   // --------------------------- Map between TAPType and DBType --------------------------------------
   private static final Map<String, DBType.DBDatatype> TAP_TO_DB =
           Map.ofEntries(
                   Map.entry("VARCHAR", DBType.DBDatatype.VARCHAR),
                   Map.entry("CHAR", DBType.DBDatatype.CHAR),
                   Map.entry("INTEGER", DBType.DBDatatype.INTEGER),
                   Map.entry("BIGINT", DBType.DBDatatype.BIGINT),
                   Map.entry("SMALLINT", DBType.DBDatatype.SMALLINT),
                   Map.entry("REAL", DBType.DBDatatype.REAL),
                   Map.entry("DOUBLE", DBType.DBDatatype.DOUBLE),
                   Map.entry("BOOLEAN", DBType.DBDatatype.SMALLINT), // ADQL workaround
                   Map.entry("BINARY", DBType.DBDatatype.BINARY),
                   Map.entry("VARBINARY", DBType.DBDatatype.VARBINARY),
                   Map.entry("BLOB", DBType.DBDatatype.BLOB),
                   Map.entry("CLOB", DBType.DBDatatype.CLOB),
                   Map.entry("TIMESTAMP", DBType.DBDatatype.TIMESTAMP),
                   Map.entry("POINT", DBType.DBDatatype.POINT),
                   Map.entry("REGION", DBType.DBDatatype.REGION)
           );

   private static DBType.DBDatatype db(String type) {
      DBType.DBDatatype resolved = TAP_TO_DB.get(type);

      if (resolved == null) {
         log.warn("Unsupported TAPType {} - defaulting to VARCHAR", type);
         return DBType.DBDatatype.VARCHAR;
      }

      return resolved;
   }

   /**
    * Maps a TAP datatype to an ADQLLib database type.
    *
    * @param tapType TAP datatype.
    * @return ADQLLib database type.
    */
   public static DBType mapDbType(String tapType) {
      return new DBType(db(tapType));
   }

   // --------------------------- Map between TAPType and SQL type --------------------------------------

   //FIXME below would be better done with hibernate Metadata mapping and schema creation from an XML definition - so that differences between databases are automatically accounted for with Dialects
   private static String toSql(DBType.DBDatatype type) {
      return switch (type) {
         case DOUBLE -> "DOUBLE PRECISION";
         case BLOB, BINARY, VARBINARY -> "BYTEA";
         case CLOB -> "TEXT";
         case POINT, REGION, CIRCLE, POLYGON -> "TEXT";
         default -> type.toString();
      };
   }

   /**
    * Maps a TAP datatype to an SQL type string.
    *
    * @param tapType TAP datatype.
    * @return SQL type name suitable for DDL generation.
    */
   public static String mapTAPTypeToSqlType(String tapType) {
      return toSql(db(tapType));
   }
}
