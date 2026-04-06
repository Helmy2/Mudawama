plugins {
    `kotlin-dsl`
}

java {
    val jvmVersion = JavaVersion.VERSION_17
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Makes the version catalog accessible in convention plugins via `the<LibrariesForLibs>()`
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.plugins.androidLibrary.toDependency())
    implementation(libs.plugins.kotlinMultiplatform.toDependency())
    implementation(libs.plugins.composeMultiplatform.toDependency())
    implementation(libs.plugins.composeCompiler.toDependency())
    implementation(libs.plugins.androidKotlinMultiplatformLibrary.toDependency())
    implementation(libs.plugins.androidLint.toDependency())
    implementation(libs.plugins.kotlinxSerialization.toDependency())
    implementation(libs.plugins.ksp.toDependency())
    implementation(libs.plugins.room.toDependency())
}

fun Provider<PluginDependency>.toDependency(): Provider<String> =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

gradlePlugin {
    plugins {
        register("kmp") {
            id = "mudawama.kmp"
            implementationClass = "KmpConventionPlugin"
        }
        register("kmpCompose") {
            id = "mudawama.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpKoin") {
            id = "mudawama.kmp.koin"
            implementationClass = "KmpKoinConventionPlugin"
        }
    }
}
