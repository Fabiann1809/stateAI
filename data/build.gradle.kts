plugins {
    alias(libs.plugins.stateai.android.library)
}

dependencies {
    api(project(":core-domain"))
    implementation(libs.kotlinx.coroutines.core)
    api(libs.androidx.datastore.preferences)
    testImplementation(libs.kotlinx.coroutines.test)
}
