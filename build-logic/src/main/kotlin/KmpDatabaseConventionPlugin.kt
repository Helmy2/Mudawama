import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpDatabaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("mudawama.kmp.library")
            pluginManager.apply("mudawama.kmp.koin")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("androidx.room")

            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

            pluginManager.withPlugin("androidx.room") {
                extensions.configure<RoomExtension> {
                    schemaDirectory("${project.projectDir}/schemas")
                }
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.androidx.room.runtime)
                    implementation(libs.androidx.sqlite.bundled)
                    implementation(libs.kotlinx.coroutines.core)
                }
            }

            dependencies {
                add("kspAndroid", libs.androidx.room.compiler)
                add("kspIosSimulatorArm64", libs.androidx.room.compiler)
                add("kspIosX64", libs.androidx.room.compiler)
                add("kspIosArm64", libs.androidx.room.compiler)
            }
        }
    }
}

