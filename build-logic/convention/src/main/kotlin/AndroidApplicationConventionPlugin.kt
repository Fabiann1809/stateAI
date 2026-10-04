import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** The Wear OS application module. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        configureQuality()
        configureUnitTesting()

        extensions.configure<ApplicationExtension> {
            namespace = BASE_NAMESPACE
            configureAndroidCommon(this)
            buildFeatures.buildConfig = true
            defaultConfig {
                applicationId = BASE_NAMESPACE
                targetSdk = libs.version("targetSdk").toInt()
                versionCode = 1
                versionName = "0.1.0"
            }
        }
    }
}
