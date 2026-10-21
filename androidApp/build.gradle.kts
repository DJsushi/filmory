import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val tmdbApiKey: String = Properties()
    .apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
    .getProperty("TMDB_API_KEY", "")

// version.txt is owned by release-please. Do not edit it by hand.
val appVersion: String = rootProject.file("version.txt").readText().trim()
val appVersionCode: Int = appVersion.split(".").map(String::toInt)
    .let { (major, minor, patch) -> major * 1_000_000 + minor * 1_000 + patch }
    .coerceAtLeast(1)

android {
    namespace = "dev.gaens.filmory"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.gaens.filmory"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersion

        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
        // Fully optimized (R8 over all packages, resources shrunk). This is the build the release workflow
        // publishes. Signed with the debug key until a release key exists. Install with `installOptimized`.
        create("optimized") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = true
                packageScope = setOf("**")
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
