import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Pure Kotlin module without Android dependencies, tested with JUnit on the JVM. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        configureQuality()

        val jvmTarget = libs.version("jvmTarget")
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.toVersion(jvmTarget)
            targetCompatibility = JavaVersion.toVersion(jvmTarget)
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(jvmTarget))
        }

        dependencies {
            add("testImplementation", platform(libs.library("junit-bom")))
            add("testImplementation", libs.library("junit-jupiter"))
            add("testRuntimeOnly", libs.library("junit-platform-launcher"))
        }
        tasks.withType<Test>().configureEach { useJUnitPlatform() }
    }
}
