plugins {
    id("mudawama.kmp.library")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.time"
    }

    configureIosFramework("MudawamaCoreTime")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

// SC-002: Clock.System must ONLY appear in SystemTimeProvider.kt.
// Verify compliance with:
//   git grep -rn "Clock\.System" --include="*.kt" -- shared/
// Expected: exactly one match in shared/core/time/src/commonMain/kotlin/.../SystemTimeProvider.kt

