plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "dev.gaens.filmory.shared"
        compileSdk = 37
        minSdk = 24

        androidResources { enable = true }
        withHostTest { }
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.navigation.compose)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    // One KSP configuration per target; Room generates the database code for each.
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "dev.gaens.filmory.resources"
    generateResClass = always
}
