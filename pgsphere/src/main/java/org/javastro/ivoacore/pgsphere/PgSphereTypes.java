/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 17/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public class PgSphereTypes {
// Note that these need to be distinct numbers from either @link org.hibernate.type.SqlTypes and java.sql.Types
    public static int POINT=8000;
    public static int CIRCLE=8001;
    public static int LINE=8002;
    public static int ELLIPSE=8003;
    public static int POLYGON=8004;
    public static int BOX=8005;
}
