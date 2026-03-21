plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    android {
        namespace = "io.github.helmy2.mudawama.core.presentation"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 30
    }

    val xcfName = "shared:core:presentationKit"

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
                implementation(libs.compose.runtime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.jetbrains.lifecycle.viewmodel)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        iosMain {
            dependencies {

            }
        }
    }

}