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
}