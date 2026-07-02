plugins {
    id("ivoacore-conventions")
    id("org.kordamp.gradle.jandex") version "2.3.0"

}
description = "IVOA UWS server library"
dependencies {
    implementation(project(":common"))
    api("org.javastro.ivoa:ivoa-entities")
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    //Database persistence layer
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    testImplementation("com.h2database:h2:2.3.232") // for database testing

}

//gradle insists on these next two - though I do not think that it really does depend on it...
tasks.withType<Javadoc>() {
    mustRunAfter("jandex")
}
tasks.named<JavaCompile>("compileTestJava") {
    mustRunAfter("jandex")
}
