plugins {
    alias(libs.plugins.stateai.android.application)
    alias(libs.plugins.stateai.android.compose)
}

/** Sensor source: "simulated" (default) or "health" for real Health Services sensors. */
val sensorSource = providers.gradleProperty("stateai.sensorSource").getOrElse("simulated")

/** State classifier: "rules" (default) or "model" for the bundled TFLite model with rule fallback. */
val classifier = providers.gradleProperty("stateai.classifier").getOrElse("rules")

android {
    defaultConfig {
        buildConfigField("boolean", "USE_HEALTH_SERVICES", (sensorSource == "health").toString())
        buildConfigField("boolean", "USE_MODEL_CLASSIFIER", (classifier == "model").toString())
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":data"))
    implementation(project(":haptics"))
    implementation(project(":sensors"))
    implementation(project(":ml"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear)
    implementation(libs.wear.ongoing)
    implementation(libs.wear.input)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.kotlinx.coroutines.test)
}
