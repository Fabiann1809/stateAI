import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Android library module with the shared SDK and Java settings. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        configureQuality()

        extensions.configure<LibraryExtension> {
            namespace = "$BASE_NAMESPACE.${namespaceSuffix()}"
            configureAndroidCommon(this)
        }
    }
}
