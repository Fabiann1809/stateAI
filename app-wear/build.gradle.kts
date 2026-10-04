plugins {
    alias(libs.plugins.stateai.android.application)
    alias(libs.plugins.stateai.android.compose)
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear)
    implementation(libs.wear.ongoing)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.kotlinx.coroutines.test)
}
