plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlinxSerialization)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.database"
    }

    configureIosFramework("shared:core:databaseKit")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}
