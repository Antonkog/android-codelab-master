import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sap.codelab"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "com.sap.codelab"
        minSdk = 27
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // AndroidX core UI
    implementation(libs.bundles.androidx.ui.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Room persistence
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Koin DI
    implementation(libs.bundles.koin)

    // Tests
    testImplementation(libs.junit4)
    androidTestImplementation(libs.bundles.androidTest)
}