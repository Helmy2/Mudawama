plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLint)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MudawamaCore"
            isStatic = true

            export(projects.shared.core.domain)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
            }
        }
    }
}