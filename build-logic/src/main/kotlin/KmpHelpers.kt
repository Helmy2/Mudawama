import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
