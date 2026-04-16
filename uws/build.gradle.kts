plugins {
    id("ivoacore-conventions")
}
description = "IVOA UWS server library"
dependencies {
    implementation(project(":common"))
    api("org.javastro.ivoa:ivoa-entities")
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    //Database persistence layer
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("org.hibernate.orm:hibernate-core:7.3.1.Final")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.postgresql:postgresql:42.7.10")

    testRuntimeOnly("com.h2database:h2:2.3.232")
}