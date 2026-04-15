plugins {
    id("ivoacore-conventions")
}
description = "IVOA UWS server library"
dependencies {
    implementation(project(":common"))
    api("org.javastro.ivoa:ivoa-entities")
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    //Database persistance layer
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}