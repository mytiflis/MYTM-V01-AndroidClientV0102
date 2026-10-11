plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.sgc.teknisyen"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sgc.teknisyen"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3303/api/\"")
        buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
    }

    signingConfigs {
        create("release") {
            val ks = System.getenv("SGC_KEYSTORE")
            if (!ks.isNullOrBlank()) {
                storeFile = file(ks)
                storePassword = System.getenv("SGC_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SGC_KEY_ALIAS")
                keyPassword = System.getenv("SGC_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val ks = System.getenv("SGC_KEYSTORE")
            if (!ks.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,NOTICE}*"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.savedstate)

    // Compose BOM + components
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Coroutines
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime)

    // Google Play Services Location
    implementation(libs.play.services.location)

    // Security (EncryptedSharedPreferences)
    implementation(libs.security.crypto)

    // Coil
    implementation(libs.coil.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
}