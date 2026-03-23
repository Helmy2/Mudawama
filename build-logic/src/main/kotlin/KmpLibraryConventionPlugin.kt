import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("com.android.lint")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(17)

                android {
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
                )
            }
        }
    }

    private fun KotlinMultiplatformExtension.android(configure: com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension.() -> Unit) {
        (this as ExtensionAware).extensions.configure("android", configure)
    }
}
