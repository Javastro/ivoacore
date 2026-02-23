/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.javastro.ivoacore.pgsphere.types.Ellipse;
import org.javastro.ivoacore.pgsphere.types.Point;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EllipseJavaType extends AbstractPgSphereJavaType<Ellipse> {


    public static final EllipseJavaType INSTANCE = new EllipseJavaType(Ellipse.class);
    protected EllipseJavaType(Type type) {
        super(type);
    }

   @Override
   public String asString(Ellipse e) { // FIXME there would appear to be a PGSphere bug when creating ellipses using this "character" cast - the second coordinate of the centre point is made -ve
      return "< {"+ e.getMajor_axis()+ ","+e.getMinor_axis() + "}," +
            PointJavaType.INSTANCE.asString(e.getCenter()) + ","+ e.getPos_angle()+ ">";
   }

   @Override
   public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
       return EllipseJDBCType.INSTANCE;
   }

   private static final Pattern re = Pattern.compile("<\\s*\\{\\s*([^,]+),\\s*([^}\\s]+)\\s*\\}\\s*,\\s*\\(\\s*([^,\\s]+)\\s*,\\s*([^\\s)]+)\\s*\\)\\s*,\\s*([^>\\s]+)\\s*>");

   @Override
   public  Ellipse fromString(CharSequence value) {
      Matcher m = re.matcher(value);
      if (m.matches()) {
         return new Ellipse(Double.parseDouble(m.group(1)),Double.parseDouble(m.group(2)),
               new Point(Double.parseDouble(m.group(3)), Double.parseDouble(m.group(4))),
               Double.parseDouble(m.group(5)));
      } else {
         return null;
      }
   }






}