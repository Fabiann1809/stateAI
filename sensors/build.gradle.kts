plugins {
    alias(libs.plugins.stateai.android.library)
}

dependencies {
    api(project(":core-domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.health.services.client)
    implementation(libs.guava.listenablefuture)
    testImplementation(libs.kotlinx.coroutines.test)
}
