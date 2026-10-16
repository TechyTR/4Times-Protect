plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val envVersionCode = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 1
val envVersionName = System.getenv("RELEASE_VERSION_NAME") ?: "1.0.0"

android {
    namespace = "com.zenithguard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zenithguard"
        minSdk = 26
        targetSdk = 34
        versionCode = envVersionCode
        versionName = envVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("keystore/zenithguard.jks")
            storeFile = keystoreFile
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "ZenithGuard123!"
            keyAlias = System.getenv("KEY_ALIAS") ?: "zenithguard_key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "ZenithGuard123!"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildFeatures {
        compose = true
        aidl = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.compose.material3)

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
