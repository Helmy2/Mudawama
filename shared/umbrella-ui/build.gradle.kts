plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella"
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
            baseName = "MudawamaUI"
            isStatic = true

            export(projects.shared.core.domain)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                implementation(projects.shared.core.data)
                implementation(projects.shared.designsystem)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }

        iosMain {
            dependencies {

            }
        }
    }

}