plugins {
    `java-library`
    `jvm-test-suite`
    `maven-publish`
    signing
}

group = "org.javastro.ivoa.core"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url= uri("https://central.sonatype.com/repository/maven-snapshots/")
    }

    //TODO - Vollt TAP dependencies from our repo (updated to Jakarta)
    maven {
        url= uri("https://repo.dev.uksrc.org/repository/maven-snapshots/")
    }

}

dependencies {
    implementation(platform("org.javastro:bom:2025.4"))
    implementation("org.slf4j:slf4j-api")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                runtimeOnly("org.slf4j:slf4j-simple")
            }
        }
    }
}
// This is needed because jvm-test-suites does not extend the testIntegration configuration from
// the base configuration, even when integrationTest is defined with "dependencies { implementation project() }"
configurations.named("integrationTestImplementation").configure {
    extendsFrom(configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME))
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).tags("TODO:a:\"To Do:\".")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            versionMapping {

                usage("java-api") {
                    fromResolutionResult()
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
                pom {
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("pahjbo")
                            name.set("Paul Harrison")
                            email.set("paul.harrison@manchester.ac.uk")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/javastro/ivoacore.git")
                        developerConnection.set("scm:git:ssh://github.com/javastro/ivoacore.git")
                        url.set("https://github.com/javastro/ivoacore")
                    }    
                    
                    
                    dependencies.artifactTypes.removeIf { d -> d.name == "bom" } 

// this removes from configurations - which we do not want as then it will not compile   https://stackoverflow.com/questions/68470193/exclude-dependency-from-pom-using-maven-publish-plugin
//                        setOf("apiElements", "runtimeElements")
//                            .flatMap { configName -> configurations[configName].hierarchy }
//                            .forEach { configuration ->
//                                configuration.dependencies.removeIf { dependency ->
//                                    dependency.name == "bom"
//                                }
//                            }

                }
            }
        }
    }


tasks.withType<GenerateModuleMetadata> {
    // gradle does not like to publish the 'enforced-platform' and so warns against it
    // not sure if it is harmful in our case - have changed to just platform to avoid the warning.
    // perhaps look
    // https://dev.to/mfvanek/creation-and-usage-of-bom-in-gradle-ca1
    //suppressedValidationErrors.add("enforced-platform")
}
