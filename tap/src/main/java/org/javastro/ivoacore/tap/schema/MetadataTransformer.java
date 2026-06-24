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

   //---------------------------------------- Map from class to dbType -----------------------------
   private record TypeMapping(
           Predicate<Class<?>> matcher,
           TAPType tapType) {
   }

   private static final List<TypeMapping> CLASS_TO_TAP = List.of(
           new TypeMapping(Objects::isNull, TAPType.VARCHAR),
           new TypeMapping(c -> String.class.isAssignableFrom(c) || Character.class.isAssignableFrom(c), TAPType.VARCHAR),
           new TypeMapping(c -> Integer.class.isAssignableFrom(c) || c == int.class, TAPType.INTEGER),
           new TypeMapping(c -> Long.class.isAssignableFrom(c) || c == long.class, TAPType.BIGINT),
           new TypeMapping(c -> Double.class.isAssignableFrom(c) || c == double.class, TAPType.DOUBLE),
           new TypeMapping(c -> Float.class.isAssignableFrom(c) || c == float.class, TAPType.REAL),
           new TypeMapping(c -> Boolean.class.isAssignableFrom(c) || c == boolean.class, TAPType.BOOLEAN),
           new TypeMapping(c -> Short.class.isAssignableFrom(c) || c == short.class, TAPType.SMALLINT),
           new TypeMapping(byte[].class::isAssignableFrom, TAPType.VARBINARY), new TypeMapping(BigDecimal.class::isAssignableFrom, TAPType.DOUBLE),
           new TypeMapping(c -> java.sql.Timestamp.class.isAssignableFrom(c) || java.util.Date.class.isAssignableFrom(c), TAPType.TIMESTAMP)
   );

   public static TAPType mapContentClassToTAPType(Class<?> contentClass) {
      return CLASS_TO_TAP.stream()
              .filter(m -> m.matcher().test(contentClass))
              .map(TypeMapping::tapType)
              .findFirst()
              .orElseGet(() -> {
                 log.warn("Unknown content class {}, defaulting to VARCHAR",
                         contentClass != null ? contentClass.getName() : "null");
                 return TAPType.VARCHAR;
              });
   }

   // --------------------------- Map between TAPType and DBType --------------------------------------
   private static final Map<TAPType, DBType.DBDatatype> TAP_TO_DB =
           Map.ofEntries(
                   Map.entry(TAPType.VARCHAR, DBType.DBDatatype.VARCHAR),
                   Map.entry(TAPType.CHAR, DBType.DBDatatype.CHAR),
                   Map.entry(TAPType.INTEGER, DBType.DBDatatype.INTEGER),
                   Map.entry(TAPType.BIGINT, DBType.DBDatatype.BIGINT),
                   Map.entry(TAPType.SMALLINT, DBType.DBDatatype.SMALLINT),
                   Map.entry(TAPType.REAL, DBType.DBDatatype.REAL),
                   Map.entry(TAPType.DOUBLE, DBType.DBDatatype.DOUBLE),
                   Map.entry(TAPType.BOOLEAN, DBType.DBDatatype.SMALLINT), // ADQL workaround
                   Map.entry(TAPType.BINARY, DBType.DBDatatype.BINARY),
                   Map.entry(TAPType.VARBINARY, DBType.DBDatatype.VARBINARY),
                   Map.entry(TAPType.BLOB, DBType.DBDatatype.BLOB),
                   Map.entry(TAPType.CLOB, DBType.DBDatatype.CLOB),
                   Map.entry(TAPType.TIMESTAMP, DBType.DBDatatype.TIMESTAMP),
                   Map.entry(TAPType.POINT, DBType.DBDatatype.POINT),
                   Map.entry(TAPType.REGION, DBType.DBDatatype.REGION)
           );

   private static DBType.DBDatatype db(TAPType type) {
      DBType.DBDatatype resolved = TAP_TO_DB.get(type);

      if (resolved == null) {
         log.warn("Unsupported TAPType {} - defaulting to VARCHAR", type);
         return DBType.DBDatatype.VARCHAR;
      }

      return resolved;
   }

   public static DBType mapDbType(TAPType tapType) {
      return new DBType(db(tapType));
   }

   // --------------------------- Map between TAPType and SQL type --------------------------------------
   private static String toSql(DBType.DBDatatype type) {
      return switch (type) {
         case DOUBLE -> "DOUBLE PRECISION";
         case BLOB, BINARY, VARBINARY -> "BYTEA";
         case CLOB -> "TEXT";
         case POINT, REGION, CIRCLE, POLYGON -> "TEXT";
         default -> type.toString();
      };
   }

   public static String mapTAPTypeToSqlType(TAPType tapType) {
      return toSql(db(tapType));
   }
}
