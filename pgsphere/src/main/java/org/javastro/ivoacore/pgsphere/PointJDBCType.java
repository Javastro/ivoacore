/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 16/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.hibernate.type.descriptor.jdbc.JdbcType;

//@JdbcTypeRegistration(PointJDBCType.class)
public class PointJDBCType  extends AbstractJDBCType implements JdbcType {
   public static final JdbcType INSTANCE = new PointJDBCType();

   @Override
   public int getDdlTypeCode() {
      return PgSphereTypes.POINT;
   }

   @Override
   public String getPGSphereTypeName() {
      return "spoint";
   }
}
