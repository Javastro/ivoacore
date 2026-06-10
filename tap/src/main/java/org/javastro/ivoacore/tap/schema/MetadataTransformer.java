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
import uk.ac.starlink.util.Pair;

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

   private record TapTypeInfo(
           DBType.DBDatatype dbType,
           String sqlType) {
   }

   private static final Map<TAPType, TapTypeInfo> TAP_TYPE_INFO =
           Map.ofEntries(
                   Map.entry(TAPType.VARCHAR,   new TapTypeInfo(DBType.DBDatatype.VARCHAR,   "VARCHAR")),
                   Map.entry(TAPType.CHAR,      new TapTypeInfo(DBType.DBDatatype.CHAR,      "CHAR")),
                   Map.entry(TAPType.INTEGER,   new TapTypeInfo(DBType.DBDatatype.INTEGER,   "INTEGER")),
                   Map.entry(TAPType.BIGINT,    new TapTypeInfo(DBType.DBDatatype.BIGINT,    "BIGINT")),
                   Map.entry(TAPType.SMALLINT,  new TapTypeInfo(DBType.DBDatatype.SMALLINT,  "SMALLINT")),
                   Map.entry(TAPType.REAL,      new TapTypeInfo(DBType.DBDatatype.REAL,      "REAL")),
                   Map.entry(TAPType.DOUBLE,    new TapTypeInfo(DBType.DBDatatype.DOUBLE,    "DOUBLE PRECISION")),
                   Map.entry(TAPType.BOOLEAN,   new TapTypeInfo(DBType.DBDatatype.SMALLINT,  "BOOLEAN")), // ADQLLib workaround
                   Map.entry(TAPType.BINARY,    new TapTypeInfo(DBType.DBDatatype.BINARY,    "BYTEA")),
                   Map.entry(TAPType.VARBINARY, new TapTypeInfo(DBType.DBDatatype.VARBINARY, "BYTEA")),
                   Map.entry(TAPType.BLOB,      new TapTypeInfo(DBType.DBDatatype.BLOB,      "BYTEA")),
                   Map.entry(TAPType.CLOB,      new TapTypeInfo(DBType.DBDatatype.CLOB,      "TEXT")),
                   Map.entry(TAPType.TIMESTAMP, new TapTypeInfo(DBType.DBDatatype.TIMESTAMP, "TIMESTAMP")),
                   Map.entry(TAPType.POINT,     new TapTypeInfo(DBType.DBDatatype.POINT,     "TEXT")),
                   Map.entry(TAPType.REGION,    new TapTypeInfo(DBType.DBDatatype.REGION,    "TEXT"))
           );

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

   public static DBType mapDbType(TAPType tapType) {
      TapTypeInfo info = TAP_TYPE_INFO.get(tapType);

      if (info == null) {
         log.warn("Unsupported TAPType {} - defaulting to VARCHAR", tapType);
         return new DBType(DBType.DBDatatype.VARCHAR);
      }

      return new DBType(info.dbType());
   }

   public static String mapTAPTypeToSqlType(TAPType tapType) {
      TapTypeInfo info = TAP_TYPE_INFO.get(tapType);

      if (info == null) {
         log.warn("Unsupported TAPType {} - defaulting to VARCHAR", tapType);
         return "VARCHAR";
      }

      return info.sqlType();
   }

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
}
