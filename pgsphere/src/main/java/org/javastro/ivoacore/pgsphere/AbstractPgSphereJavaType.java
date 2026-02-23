/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 20/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractJavaType;
import org.javastro.ivoacore.pgsphere.types.Point;
import org.postgresql.util.PGobject;

import java.lang.reflect.Type;

/**
 * Base Cladd for all of the JavaTypes in PGSphere.
 * @param <S> The actual PGSphere type.
 */
public abstract class AbstractPgSphereJavaType<S> extends AbstractJavaType<S> {
   /**
    * Constructor.
    * @param type the type.
    */
   protected AbstractPgSphereJavaType(Type type) {
      super(type);
   }

   // next two functions localize the string handling to the javaType ( rather than forcing toString on the actual type )
   @Override
   public String toString(S value) {
      return asString(value);
   }

   /**
    * create a string representation of the type.
    * @param p An instance of the type.
    * @return the string representation.
    */
   public abstract String asString(S p);

   @Override
   public <X> S wrap(X value, WrapperOptions options) {
      if ( value == null ) {
         return null;
      }
      if (value instanceof CharSequence cs) {
         return fromString(cs);
      }
      if (value instanceof PGobject pgo) {
         return fromString(pgo.getValue());
      }
      throw unknownWrap( value.getClass() );
   }

   @Override
   public <X> X unwrap(S value, Class<X> type, WrapperOptions options) {
      if(value == null)
      {
         return null;
      }
      if ( String.class.isAssignableFrom( type ) ) {
         return (X) asString( value );
      }
      throw unknownUnwrap( type );
   }
}
