plugins {
    id("mudawama.kmp.library")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.domain"
    }

    configureIosFramework("shared:core:domainKit")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}