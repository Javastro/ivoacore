/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 16/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * Circle JDBCType.
 */
public class CircleJDBCType extends AbstractJDBCType implements JdbcType {
   /**
    * the single necessary instance.
    */
   public static final JdbcType INSTANCE = new CircleJDBCType();

   @Override
   public int getDdlTypeCode() {
      return PgSphereTypes.CIRCLE;
   }

   @Override
   public String getPGSphereTypeName() {
      return "scircle";
   }
}
