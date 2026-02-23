/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoacore.pgsphere;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;

/**
 * A hibernate dialect specifically for working with PGSphere.
 */
public class PgSphereDialect extends PostgreSQLDialect {

    public PgSphereDialect() {
        super();
    }


    @Override
    public void contributeTypes(TypeContributions typeContributions, org.hibernate.service.ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        // Register the specific JDBC types used by PgSphere
        typeContributions.contributeJdbcType(PointJDBCType.INSTANCE);
        typeContributions.contributeJavaType(PointJavaType.INSTANCE);
        typeContributions.contributeJdbcType(CircleJDBCType.INSTANCE);
        typeContributions.contributeJavaType(CircleJavaType.INSTANCE);
        typeContributions.contributeJdbcType(EllipseJDBCType.INSTANCE);
        typeContributions.contributeJavaType(EllipseJavaType.INSTANCE);

    }

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        final DdlTypeRegistry ddlTypeRegistry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(PgSphereTypes.POINT,"spoint",this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(PgSphereTypes.CIRCLE, "scircle",this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(PgSphereTypes.ELLIPSE, "sellipse",this));

    }

    
    //TODO review these function definitions.
    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();

        var typeConfig = functionContributions.getTypeConfiguration();
        var doubleType = typeConfig.getBasicTypeRegistry()
              .resolve(StandardBasicTypes.DOUBLE);
        var booleanType = typeConfig.getBasicTypeRegistry()
              .resolve(StandardBasicTypes.BOOLEAN);

        // Distance function x,y <-> x,y
        functionRegistry.registerPattern(
              "pgsphere_distance",
              "(spoint(radians(?1), radians(?2))::spoint <-> spoint(radians(?3), radians(?4))::spoint)",
              doubleType
        );


        /* not needed?
        // --- Basic Constructors ---
        // Usage: spoint(long, lat)
        functionRegistry.register("spoint", new StandardSQLFunction("spoint", StandardBasicTypes.OBJECT_TYPE));
        functionRegistry.register("scircle", new StandardSQLFunction("scircle", StandardBasicTypes.OBJECT_TYPE));
        */
        
        
        // --- Measurement Functions ---
        // Usage: area(myCircle)
        functionRegistry.register("area", new StandardSQLFunction("area", StandardBasicTypes.DOUBLE));
        functionRegistry.register("circum", new StandardSQLFunction("circum", StandardBasicTypes.DOUBLE));
        functionRegistry.register("length", new StandardSQLFunction("length", StandardBasicTypes.DOUBLE));

        // --- Operator Mappings (Crucial for HQL) ---

        // Distance Operator (<->)
        // Usage in HQL: sphere_distance(point1, point2)
        functionRegistry.registerPattern(
              "sphere_distance",
              "(?1 <-> ?2)",
              doubleType
        );

        // Contained By Operator (@)
        // Usage in HQL: sphere_contained(point, circle)
        functionRegistry.registerPattern(
              "sphere_contained",
              "(?1 @ ?2)",
              booleanType
        );

        // Overlaps Operator (&&)
        // Usage in HQL: sphere_overlaps(circle1, circle2)
        functionRegistry.registerPattern(
              "sphere_overlaps",
              "(?1 && ?2)",
              booleanType
        );

        // Equal Operator (=) 
        // Note: Standard = usually works, but specific sphere equality can be mapped
        functionRegistry.registerPattern(
              "sphere_equals",
              "(?1 = ?2)",
              booleanType
        );
        
        
    }
}

 

