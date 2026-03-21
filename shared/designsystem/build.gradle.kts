plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.designsystem"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = 1
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MudawamaDesignSystem"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                implementation(libs.ui.tooling.preview)

            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific dependencies if needed
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.ui.tooling)
}

