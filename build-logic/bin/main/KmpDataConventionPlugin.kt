import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.findByType(MudawamaExtension::class.java)
                ?: extensions.create("mudawama", MudawamaExtension::class.java)

            pluginManager.apply("mudawama.kmp.library")
            pluginManager.apply("mudawama.kmp.koin")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("io.insert-koin.compiler.plugin")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.kotlinx.coroutines.core)
                        ktor(libs)
                    }

                    androidMain.dependencies {
                        implementation(libs.ktor.client.okhttp)
                    }

                    iosMain.dependencies {
                        implementation(libs.ktor.client.darwin)
                    }
                }
            }

            afterEvaluate {
                if (extension.data.useSerialization) {
                    extensions.configure<KotlinMultiplatformExtension> {
                        sourceSets.commonMain.dependencies {
                            implementation(libs.kotlinx.serialization.json)
                        }
                    }
                }

                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets.apply {
                        if (extension.data.useDataStore) {
                            commonMain.dependencies {
                                implementation(libs.androidx.datastore.preferences)
                            }
                        }

                        if (extension.data.useTink) {
                            androidMain.dependencies {
                                implementation(libs.tink.android)
                            }
                        }
                    }
                }
            }
        }
    }
}
