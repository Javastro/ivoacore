/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;


/*
 * Created on 17/02/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * Codes for the PGSphere Types.
 */
public class PgSphereTypes {
// Note that these need to be distinct numbers from either @link org.hibernate.type.SqlTypes and java.sql.Types
/** Code for point type */
    public static int POINT=8000;

/** Code for Circle type */
    public static int CIRCLE=8001;

/** Code for Line type */
    public static int LINE=8002;

/** Code for Ellipse type  */
    public static int ELLIPSE=8003;

/** Code for Polygon type */
    public static int POLYGON=8004;

/** Code for Box type */
    public static int BOX=8005;
}
