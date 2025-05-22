plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.edugo_fe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.edugo_fe"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    buildFeatures{
        viewBinding = true
        compose = true
    }

}

dependencies {


//    implementation("com.google.ar.sceneform.ux:sceneform-ux:1.15.0")
    implementation("io.github.sceneview:arsceneview:2.3.0")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation(libs.core.ktx)
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.0")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)

    // SceneView
    // ARCore
    api("com.google.ar:core:1.48.0")

}