import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/** Shared Android settings applied to every Android module. */
internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    val javaVersion = JavaVersion.toVersion(libs.version("jvmTarget"))
    extension.apply {
        compileSdk = libs.version("compileSdk").toInt()
        defaultConfig.minSdk = libs.version("minSdk").toInt()
        compileOptions.sourceCompatibility = javaVersion
        compileOptions.targetCompatibility = javaVersion
    }
}
