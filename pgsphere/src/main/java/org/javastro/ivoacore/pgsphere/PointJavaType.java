/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.javastro.ivoacore.pgsphere.types.Point;


import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//below does not seem to work.
//@JavaTypeRegistration(javaType = Point.class, descriptorClass = PointJavaType.class)

/**
 * The Point JavaType.
 */
public class PointJavaType extends AbstractPgSphereJavaType<Point> {

   /**
    * the single necessary instance.
    */
    public static final PointJavaType INSTANCE = new PointJavaType(Point.class);
   /**
    * Constructor. Use instance variable instead.
    * @param type Type.
    */
   protected PointJavaType(Type type) {
        super(type);
    }

   @Override
   public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
       return PointJDBCType.INSTANCE;
   }

   private static final Pattern re = Pattern.compile("\\(\\s*([^,\\s]+)\\s*,\\s*([^)]+)\\s*\\)");

   @Override
   public  Point fromString(CharSequence value) {
      Matcher m = re.matcher(value);
      if (m.matches()) {
         return new Point(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
      } else {
         return null;
      }
   }

   @Override
   public String asString(Point p) {
      return "(" + p.getAlpha() + ", " + p.getDelta() + ")";

   }





}