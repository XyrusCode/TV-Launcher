plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "xyruscode.tv.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "xyruscode.tv.launcher"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    // One shared, committed signing key (identity: xyruscode.tv.launcher) used by every build so
    // updates never conflict — a device installs a new version straight over the old one. The key
    // is a PKCS12 keystore, intentionally public: fine for a personally self-distributed GitHub app,
    // not Play-grade. Mirrors the Xyrus-YT-Plucker release model.
    signingConfigs {
        create("shared") {
            storeFile = rootProject.file("keystore/tvlauncher.jks")
            storePassword = "tvlauncher"
            keyAlias = "tvlauncher"
            keyPassword = "tvlauncher"
            storeType = "PKCS12"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            signingConfig = signingConfigs.getByName("shared")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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

    buildFeatures {
        compose = true
    }

    // Don't let a non-critical lint rule fail the release build in CI.
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.tv.material)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)

    implementation(libs.jellyfin.core)
}
