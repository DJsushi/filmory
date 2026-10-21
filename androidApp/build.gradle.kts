import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val tmdbApiKey: String = Properties()
    .apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
    .getProperty("TMDB_API_KEY", "")

android {
    namespace = "dev.gaens.filmory"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.gaens.filmory"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.ui.tooling)
}
