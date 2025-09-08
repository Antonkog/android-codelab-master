import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.sap.codelab"
    compileSdk = 36

    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "com.sap.codelab"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Export Room schema for auto migrations
    // This generates JSON schema files in app/schemas on build
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    kotlin {
        target {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    // Export Room schema for auto migrations
    // This generates JSON schema files in app/schemas on build
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // AndroidX core UI
    implementation(libs.bundles.androidx.ui.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Room persistence
    implementation(libs.bundles.room)
    implementation(libs.core.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Koin DI
    implementation(libs.bundles.koin)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Tests
    testImplementation(libs.bundles.test)

    // Android instrumented tests
    androidTestImplementation(libs.bundles.androidTest)
}