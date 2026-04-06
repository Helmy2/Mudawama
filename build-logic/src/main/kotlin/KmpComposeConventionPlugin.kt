import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.compose.resources.ResourcesExtension

class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("mudawama.kmp")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val modulePackage = "mudawama.${target.path.trim(':').replace(":", ".")}"
            extensions.configure<org.jetbrains.compose.ComposeExtension> {
                (this as ExtensionAware).extensions.configure<ResourcesExtension>("resources") {
                    packageOfResClass = modulePackage
                }
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Required since AGP 8.8+ with com.android.kotlin.multiplatform.library:
                // without this, .cvr files are never packaged into the Android assets.
                android {
                    androidResources.enable = true
                }
            }
        }
    }

    private fun KotlinMultiplatformExtension.android(configure: com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension.() -> Unit) {
        (this as ExtensionAware).extensions.configure("android", configure)
    }
}
