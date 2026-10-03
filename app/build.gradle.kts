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
    packaging{
        resources{
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
        versionCode = 5
        versionName = "1.4"
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

    signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_FILE"))
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")

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
