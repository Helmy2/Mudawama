import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinMultiplatformExtension.configureIosFramework(
    name: String,
    isStatic: Boolean = false,
    export: (org.jetbrains.kotlin.gradle.plugin.mpp.Framework.() -> Unit)? = null
) {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = name
            this.isStatic = isStatic
            export?.invoke(this)
        }
    }
}

fun KotlinDependencyHandler.koin(libs: org.gradle.accessors.dm.LibrariesForLibs) {
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.bundles.koin)
}

fun KotlinDependencyHandler.ktor(libs: org.gradle.accessors.dm.LibrariesForLibs) {
    implementation(libs.bundles.ktor)
}

fun KotlinDependencyHandler.lifecycle(libs: org.gradle.accessors.dm.LibrariesForLibs) {
    implementation(libs.bundles.lifecycle)
}
