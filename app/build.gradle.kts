import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.learnapp"
    compileSdk = 36

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.learnapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }
        val apiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        val apiKey2 = properties.getProperty("GEMINI_API_KEY_2") ?: ""
        val apiKey3 = properties.getProperty("GEMINI_API_KEY_3") ?: ""
        // Đưa Key vào BuildConfig để dùng trong code Kotlin
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "GEMINI_API_KEY_2", "\"$apiKey2\"")
        buildConfigField("String", "GEMINI_API_KEY_3", "\"$apiKey3\"")
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
}

dependencies {

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    kapt("androidx.room:room-compiler:2.7.2")
    
    implementation(libs.androidx.media3.exoplayer)
    implementation("androidx.media3:media3-ui:1.3.1")


    implementation(libs.firebase.crashlytics)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.firebase.auth)

    implementation ("com.github.bumptech.glide:glide:4.16.0")
//    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")


    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}