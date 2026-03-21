plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.koin.compiler)
}

kotlin {

    android {
        namespace = "io.github.helmy2.mudawama.core.data"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = 1
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    val xcfName = "shared:core:dataKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared.core.domain)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kermit)
            }
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.tink.android)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}