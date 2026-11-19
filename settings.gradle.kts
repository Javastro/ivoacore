pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.dev.uksrc.org/repository/maven-public/")
        mavenLocal()
    }

}
rootProject.name = "ivoacore"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url= uri("https://repo.dev.uksrc.org/repository/maven-public/")
        }
    }
}


plugins {
    id("org.javastro.build") version "0.2.9"
}
//only first level dirs are automatically included by javastro gradle conventions, so we need to include the submodules explicitly
include(":clients:registry-client")
include(":clients:vospace-client")
include(":clients:tap-client")
