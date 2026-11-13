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

    // Desktop JVM target: used only to run the UI tests quickly and without a device.
    jvm("desktop") {
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
            implementation(libs.compose.icons.extended)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        val desktopMain by getting
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
                implementation(libs.multiplatform.settings.test)
            }
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
    customDirectory(sourceSetName = "desktopMain", directoryProvider = packsDir)
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
        // The release workflow passes the real values: -PdiabemVersionName / -PdiabemVersionCode.
        versionCode = (providers.gradleProperty("diabemVersionCode").orNull ?: "1").toInt()
        versionName = providers.gradleProperty("diabemVersionName").orNull ?: "0.1.0-dev"
    }

    // The signing key never lives in the repository: it comes from environment variables
    // (GitHub secrets in the release workflow). Without them, release builds stay unsigned.
    val keystorePath = providers.environmentVariable("DIABEM_KEYSTORE_FILE").orNull
    val releaseSigning = if (keystorePath != null) {
        signingConfigs.create("release") {
            storeFile = file(keystorePath)
            storePassword = providers.environmentVariable("DIABEM_KEYSTORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("DIABEM_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("DIABEM_KEY_PASSWORD").get()
        }
    } else {
        null
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = releaseSigning
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Store screenshots are generated on demand: -Pdiabem.screenshots=<folder>
tasks.withType<Test>().configureEach {
    providers.gradleProperty("diabem.screenshots").orNull?.let {
        systemProperty("diabem.screenshots", rootProject.file(it).absolutePath)
        outputs.upToDateWhen { false }
    }
}
