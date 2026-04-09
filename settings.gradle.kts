rootProject.name = "Mudawama"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":shared:core:common")
include(":shared:core:domain")
include(":shared:core:data")
include(":shared:core:database")
include(":shared:core:time")
include(":shared:core:presentation")
include(":shared:umbrella-ui")
include(":shared:umbrella-core")
include(":shared:designsystem")
include(":shared:navigation")
include(":feature:habits:domain")
include(":feature:habits:data")
include(":feature:habits:presentation")
include(":feature:prayer:domain")
include(":feature:prayer:data")
include(":feature:prayer:presentation")

