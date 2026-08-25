
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


include("common")
include("dal")
include("uws")
include("tap")
include("pgsphere")
include(":clients:registry-client")
include(":clients:vospace-client")
include(":clients:tap-client")

