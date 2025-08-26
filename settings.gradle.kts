
rootProject.name = "ivoacore"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url= uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}


include("common")
include("dal") //TODO is there really a distinction from common?
include("tap")
include("uws")
include(":clients:registry")
include(":clients:vospace")
include(":clients:tap")

