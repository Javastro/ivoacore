/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 18/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.javastro.ivoacore.pgsphere.types.Shape;
import org.postgresql.util.PGobject;

import java.sql.*;

public abstract class  AbstractJDBCType  implements JdbcType {

   public abstract String getPGSphereTypeName();

   @Override
   public int getDefaultSqlTypeCode() {
      return Types.OTHER;
   }

   @Override
   public int getJdbcTypeCode() {
      return Types.OTHER;
   }

   @Override
   public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
      return new BasicBinder<X>( javaType, this ) {
         @Override
         protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
            final PGobject obj = toPGobject( value, options );
            st.setObject( index, obj );
         }

         @Override
         protected void doBind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
            final PGobject obj = toPGobject( value, options );
            st.setObject( name, obj );
         }

         private PGobject toPGobject(X value, WrapperOptions options) throws SQLException {
            final PGobject obj = new PGobject();
            obj.setType(getPGSphereTypeName());
            obj.setValue(javaType.toString(value));
            return obj;
         }
      };
   }

   @Override
   public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
      return new BasicExtractor<X>( javaType, this ) {

         @Override
         protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
            return getJavaType().wrap(rs.getObject(paramIndex),options);
         }

         @Override
         protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
            return getJavaType().wrap(statement.getObject(index),options);
         }

         @Override
         protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
            return getJavaType().wrap(statement.getObject(name),options);
         }
      };
   }
}
