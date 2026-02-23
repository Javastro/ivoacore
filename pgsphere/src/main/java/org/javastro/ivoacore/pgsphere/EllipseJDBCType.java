/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 16/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.hibernate.type.descriptor.jdbc.JdbcType;

public class EllipseJDBCType extends AbstractJDBCType implements JdbcType {
   public static final JdbcType INSTANCE = new EllipseJDBCType();

   @Override
   public int getDdlTypeCode() {
      return PgSphereTypes.ELLIPSE;
   }

   @Override
   public String getPGSphereTypeName() {
      return "sellipse";
   }
}
