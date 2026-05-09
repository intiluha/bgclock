import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val keystoreFile = rootProject.file("release.p12")
val signingTaskNames = setOf("assembleRelease", "installRelease", "bundleRelease")
val needsSigning = gradle.startParameter.taskNames.any {
    it.substringAfterLast(":") in signingTaskNames
}

fun fetchKeystorePassword(): String {
    val proc = ProcessBuilder("pass", "Dev/bgclock").start()
    val firstLine = proc.inputStream.bufferedReader().useLines { it.firstOrNull() }?.trim().orEmpty()
    val exit = proc.waitFor()
    if (exit != 0 || firstLine.isEmpty()) {
        val err = proc.errorStream.bufferedReader().readText().trim()
        error("pass Dev/bgclock failed (exit=$exit): $err")
    }
    return firstLine
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.bgclock"
    compileSdk = 35
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.bgclock"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                keyAlias = "bgclock"
                if (needsSigning) {
                    val pw = fetchKeystorePassword()
                    storePassword = pw
                    keyPassword = pw
                }
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}
