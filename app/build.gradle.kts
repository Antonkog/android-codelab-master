import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    val major = 1
    val minor = 1
    val revision = 4

    namespace = "com.sap.codelab"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.sap.codelab"
        minSdk = 27
        targetSdk = 36
        versionCode = generateVersionCode(major, minor, revision)
        versionName = generateVersionName(major, minor, revision)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Export Room schema for auto migrations
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
    buildFeatures {
        buildConfig = true
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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("roborazzi.record", "true")
        }
    }
}

dependencies {
    // Permissions (Compose Accompanist)
    implementation(libs.accompanist.permissions)
    // Google Maps (Play Services + Maps Compose)
    implementation(libs.bundles.maps)

    // Lifecycle (ViewModel, Runtime, Process)
    implementation(libs.bundles.lifecycle)

    // Common shared libs (Room, Koin, Coroutines, Ktor)
    implementation(libs.bundles.common)
    ksp(libs.room.compiler) // Room annotation processor
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // Testing
    testImplementation(libs.bundles.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.androidTest)
}

// app/versioning.gradle.kts
fun generateVersionCode(major: Int, minor: Int, revision: Int): Int {
    return major * 1_000_000 + minor * 10_000 + revision
}

fun generateVersionName(major: Int, minor: Int, revision: Int): String {
    val minorStr = minor.toString().padStart(2, '0')
    val revisionStr = revision.toString().padStart(2, '0')
    return "$major.$minorStr.$revisionStr"
}