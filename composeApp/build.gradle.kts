import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

val iosEnabled = providers.gradleProperty("diabem.ios.enabled").orNull == "true"

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("diabem")
        browser {
            commonWebpackConfig { outputFileName = "diabem.js" }
        }
        binaries.executable()
    }

    if (iosEnabled) {
        listOf(iosArm64(), iosSimulatorArm64()).forEach {
            it.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}

evaluationDependsOn(":packbuilder")
val countryPacks = project(":packbuilder").tasks.named("buildCountryPacks")

compose.resources {
    publicResClass = false
    packageOfResClass = "com.velkyss.diabem.app.resources"
    generateResClass = always
    // Country packs are generated at build time from data/ (validated, with nutrients computed).
    // They go to the platform source sets because customDirectory replaces the default folder.
    val packsDir = countryPacks.map { task ->
        layout.projectDirectory.dir(task.outputs.files.files.first { it.name == "packs" }.absolutePath)
    }
    customDirectory(sourceSetName = "androidMain", directoryProvider = packsDir)
    customDirectory(sourceSetName = "wasmJsMain", directoryProvider = packsDir)
    if (iosEnabled) {
        customDirectory(sourceSetName = "iosMain", directoryProvider = packsDir)
    }
}

android {
    namespace = "com.velkyss.diabem"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.velkyss.diabem"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
