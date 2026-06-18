plugins {
    id("ivoacore-conventions")
    id("org.kordamp.gradle.jandex") version "2.3.0"
}
description = "IVOA Spherical Geometry library"
version = "0.9.1-SNAPSHOT"

tasks.javadoc {
    dependsOn(tasks.jandex)
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")
    api("org.eclipse.microprofile.openapi:microprofile-openapi-api:2.0.1")
    api("org.javastro.ivoa.vo-dml:vodml-runtime")
    implementation("org.hibernate.orm:hibernate-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.glassfish.jaxb:jaxb-runtime")
    testImplementation("com.h2database:h2:2.3.232") // for database testing

}