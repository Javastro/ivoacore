package org.javastro.ivoacore.pgsphere;

import org.javastro.ivoacore.pgsphere.types.Point;
import org.javastro.ivoacore.pgsphere.types.Polygon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PolygonJavaTypeTest {

   @Test
   void convertsPgSpherePolygonText() {
      Polygon polygon = PolygonJavaType.INSTANCE.fromString("{(1.0, 2.0), (3.0, 4.0), (5.0, 6.0)}");

      assertEquals(List.of(new Point(1.0, 2.0), new Point(3.0, 4.0), new Point(5.0, 6.0)),
            polygon.getPoints());
      assertEquals("{(1.0, 2.0),(3.0, 4.0),(5.0, 6.0)}", PolygonJavaType.INSTANCE.asString(polygon));
   }

   @Test
   void rejectsMalformedPgSpherePolygonText() {
      assertNull(PolygonJavaType.INSTANCE.fromString("{(1.0, 2.0), invalid}"));
      assertNull(PolygonJavaType.INSTANCE.fromString("{}"));
      assertNull(PolygonJavaType.INSTANCE.fromString("{(1.0, 2.0),}"));
   }
}
