/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * Polygon JDBC type.
 */
public class PolygonJDBCType extends AbstractJDBCType implements JdbcType {
   /**
    * The single necessary instance.
    */
   public static final JdbcType INSTANCE = new PolygonJDBCType();

   @Override
   public int getDdlTypeCode() {
      return PgSphereTypes.POLYGON;
   }

   @Override
   public String getPGSphereTypeName() {
      return "spoly";
   }
}
