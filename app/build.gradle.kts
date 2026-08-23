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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
//    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
//    implementation("io.noties.markwon:ext-tables:4.6.2")
//    implementation("io.noties.markwon:syntax-highlight:4.6.2")
}