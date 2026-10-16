/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.javastro.ivoacore.pgsphere.types.Point;
import org.javastro.ivoacore.pgsphere.types.Polygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Polygon Java type.
 */
public class PolygonJavaType extends AbstractPgSphereJavaType<Polygon> {

   /**
    * The single necessary instance.
    */
   public static final PolygonJavaType INSTANCE = new PolygonJavaType(Polygon.class);

   private static final Pattern POINT_PATTERN =
         Pattern.compile("\\s*\\(\\s*([^,()\\s]+)\\s*,\\s*([^,()\\s]+)\\s*\\)");

   /**
    * Constructor. Use the instance variable instead.
    *
    * @param type the represented type
    */
   protected PolygonJavaType(Type type) {
      super(type);
   }

   @Override
   public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
      return PolygonJDBCType.INSTANCE;
   }

   @Override
   public Polygon fromString(CharSequence value) {
      String text = value.toString().trim();
      if (text.length() < 2 || text.charAt(0) != '{' || text.charAt(text.length() - 1) != '}') {
         return null;
      }

      String pointsText = text.substring(1, text.length() - 1);
      Matcher matcher = POINT_PATTERN.matcher(pointsText);
      List<Point> points = new ArrayList<>();
      int position = 0;

      while (position < pointsText.length()) {
         matcher.region(position, pointsText.length());
         if (!matcher.lookingAt()) {
            return null;
         }
         try {
            points.add(new Point(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))));
         } catch (NumberFormatException e) {
            return null;
         }
         position = matcher.end();
         while (position < pointsText.length() && Character.isWhitespace(pointsText.charAt(position))) {
            position++;
         }
         if (position < pointsText.length()) {
            if (pointsText.charAt(position) != ',') {
               return null;
            }
            position++;
            if (position == pointsText.length()) {
               return null;
            }
         }
      }

      return points.isEmpty() ? null : new Polygon(points);
   }

   @Override
   public String asString(Polygon polygon) {
      return polygon.getPoints().stream()
            .map(PointJavaType.INSTANCE::asString)
            .collect(java.util.stream.Collectors.joining(",", "{", "}"));
   }
}
