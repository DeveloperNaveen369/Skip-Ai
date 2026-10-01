plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "community.india.hack.in.skipai"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES"
            )
        }
    }

    defaultConfig {
        applicationId = "community.india.hack.in.skipai"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += "arm64-v8a"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    // Release signing is configured only when the required
    // environment variables are available.
    signingConfigs {
        val keystoreFile = System.getenv("KEYSTORE_FILE")
        val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
        val keyAliasValue = System.getenv("KEY_ALIAS")
        val keyPasswordValue = System.getenv("KEY_PASSWORD")

        if (
            keystoreFile != null &&
            keystorePassword != null &&
            keyAliasValue != null &&
            keyPasswordValue != null
        ) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        release {
            // Use the release signing config only when it exists.
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation("io.noties.markwon:core:4.6.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("androidx.room:room-runtime:2.8.1")
    annotationProcessor ("androidx.room:room-compiler:2.8.1")

//    implementation("dev.ffmpegkit-maintained:llama-android:0.1.1")

    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.google.mediapipe:tasks-genai:0.10.24")
}
// implementation("io.noties.markwon:ext-strikethrough:4.6.2")
// implementation("io.noties.markwon:ext-tables:4.6.2")
// implementation("io.noties.markwon:syntax-highlight:4.6.2")