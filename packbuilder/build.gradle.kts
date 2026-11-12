plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlin.test)
}

application {
    mainClass.set("com.velkyss.diabem.packbuilder.MainKt")
}

val dataDir = rootProject.layout.projectDirectory.dir("data")
val packsOut = layout.buildDirectory.dir("generated/packs")
val reportsOut = layout.buildDirectory.dir("reports/packs")

/**
 * Validates every country pack in data/ and writes the compiled packs.
 * Fails the build if any recipe or reference is wrong.
 */
val buildCountryPacks by tasks.registering(JavaExec::class) {
    group = "diabem"
    description = "Validates data/ and compiles country packs with nutrients per serving."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.velkyss.diabem.packbuilder.MainKt")
    inputs.dir(dataDir)
    outputs.dir(packsOut)
    outputs.dir(reportsOut)
    argumentProviders += CommandLineArgumentProvider {
        listOf(dataDir.asFile.absolutePath, packsOut.get().asFile.absolutePath, reportsOut.get().asFile.absolutePath)
    }
}

tasks.test {
    systemProperty("diabem.dataDir", dataDir.asFile.absolutePath)
}

tasks.named("check") { dependsOn(buildCountryPacks) }
