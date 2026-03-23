import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpPresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.findByType(MudawamaExtension::class.java)
                ?: extensions.create("mudawama", MudawamaExtension::class.java)

            // Always apply the base KMP/Android plugins eagerly
            pluginManager.apply("mudawama.kmp.library")
            pluginManager.apply("mudawama.kmp.koin")
            
            // Apply Compose framework plugins eagerly to avoid "too late" errors
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("io.insert-koin.compiler.plugin")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.kotlinx.coroutines.core)
                        lifecycle(libs)
                    }
                }
            }

            afterEvaluate {
                if (extension.presentation.useCompose) {
                    extensions.configure<KotlinMultiplatformExtension> {
                        sourceSets.apply {
                            commonMain.dependencies {
                                implementation(libs.bundles.compose)
                            }
                            androidMain.dependencies {
                                implementation(libs.androidx.activity.compose)
                                implementation(libs.ui.tooling)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun KotlinMultiplatformExtension.android(configure: com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension.() -> Unit) {
        (this as ExtensionAware).extensions.configure("android", configure)
    }
}
