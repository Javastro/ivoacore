
rootProject.name = "ivoacore"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url= uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}


include("common")
include("dal")
include("tap")
include("uws")
include(":clients:registry")
include(":clients:vospace")
include(":clients:tap")

