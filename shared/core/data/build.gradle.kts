plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.data"
    }

    configureIosFramework("shared:core:dataKit")

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktor)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.datastore.preferences)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.play.services.location)
                implementation(libs.kotlinx.coroutines.play.services)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.tink.android)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
