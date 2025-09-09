
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
include("uws")
include("tap")
include(":clients:registry")
include(":clients:vospace")
include(":clients:tap")

