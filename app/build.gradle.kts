// Import necessary libraries for reading properties
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" // KSP plug-in
}

android {
    namespace = "com.cos407.cs407finalproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cos407.cs407finalproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load the API Key in local.properties
        val apiKey: String = project.rootProject.file("local.properties").inputStream().use {
            val properties = Properties()
            properties.load(it)
            properties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
        }
        // Define MAPS_API_KEY to BuildConfig
        buildConfigField("String", "MAPS_API_KEY", "\"$apiKey\"")
        // Add this line to ensure that AndroidManifest.xml can use MAPS_API_KEY.
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Enable buildConfig generation
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room database dependencies with KSP
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler.v261)

    // Google Places API & Maps SDK dependencies
    implementation(libs.google.places)
    implementation(libs.google.maps)

    // Kotlin Coroutines for concurrency
    implementation(libs.kotlinx.coroutines.android)
}

ksp {
    // KSP arguments for Room schema location and incremental processing
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
