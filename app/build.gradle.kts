plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Ensure kapt is included
    id("dagger.hilt.android.plugin") // Hilt plugin must be here
}


android {
    namespace = "com.hady.robustexoplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hady.robustexoplayer"
        minSdk = 24
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Jetpack Compose Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 📌 ExoPlayer - Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.database)
    implementation(libs.media3.datasource)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.smoothstreaming) // Keep only one instance of smooth streaming
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayer.ima)
    implementation(libs.media3.datasource.rtmp)
    implementation(libs.media3.exoplayer.rtsp)

    // 📌 Hilt Dependencies (Updated to latest version)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // 📌 Hilt for Jetpack Compose
    implementation(libs.androidx.hilt.navigation.compose)
}


// Allow references to generated code
kapt {
    correctErrorTypes = true
}