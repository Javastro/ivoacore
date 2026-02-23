/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.javastro.ivoacore.pgsphere.types.Circle;
import org.javastro.ivoacore.pgsphere.types.Point;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Circle Java Type.
 */
public class CircleJavaType extends AbstractPgSphereJavaType<Circle> {

   /**
    * the single necessary instance.
    */
    public static final CircleJavaType INSTANCE = new CircleJavaType(Circle.class);

   /**
    * Constructor. Use instance variable instead.
    * @param type Type.
    */
    protected CircleJavaType(Type type) {
        super(type);
    }

   @Override
   public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
       return CircleJDBCType.INSTANCE;
   }

   private static final Pattern re = Pattern.compile("<\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([^\\s)]+)\\s*\\)\\s*,\\s*([^>\\s]+)\\s*>");

   @Override
   public  Circle fromString(CharSequence value) {
      Matcher m = re.matcher(value);
      if (m.matches()) {
         return  new Circle( new Point(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2))),Double.parseDouble(m.group(3)));
      } else {
         return null;
      }
   }

   @Override
   public String asString(Circle c) {
      return "<(" + c.getCenter().getAlpha() + ", " + c.getCenter().getDelta() + "),"+c.getRadius()+">";

   }





}