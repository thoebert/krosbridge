import com.github.thoebert.krosbridgecodegen.KROSBridgeCodegenPluginConfig
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    //kotlin("jvm") version "1.9.0"
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.androidLibrary)

    id("io.github.thoebert.krosbridge-codegen") version "1.0.6"
    `maven-publish`
}

group = "com.github.thoebert"
version = "1.0"

val osName = System.getProperty("os.name")
val hostOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var hostArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val host = "${hostOs}-${hostArch}"

var skiaVersion = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    skiaVersion = project.properties["skiko.version"] as String
}

kotlin {


    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    //@OptIn(ExperimentalWasmDsl::class)
    wasmJs()

    sourceSets {

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }


        jvmMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        jvmTest {

            dependencies {
                implementation(libs.jakarta.json)
                implementation(libs.jul.to.slf4j)
                implementation(libs.log4j.core)
                implementation(libs.log4j.slf4j.impl)
                implementation(libs.tyrus.server)
                implementation(libs.tyrus.container.grizzly.server)
                implementation(libs.jackson.dataformat.yaml) // YAML Config
                implementation(libs.jackson.module.kotlin)
                implementation("org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:$skiaVersion")

            }
        }

        commonMain {
            kotlin.srcDirs("${buildDir}/generated/source/ros")
            dependencies {
                implementation(libs.napier)
                implementation("com.ashampoo:kim:0.15.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
                implementation(libs.ktor.client)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.neogation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.coroutines)
                implementation("org.jetbrains.skiko:skiko:$skiaVersion")

            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)

            }

        }
    }


}


// tasks.named("build") { dependsOn("generateROSSources") }


configure<KROSBridgeCodegenPluginConfig> {
    packageName.set("com.github.thoebert.krosbridge.messages")
}



publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = project.name
            version = version
            // from(components["java"])
        }
    }
}

android {
    namespace = "com.github.thoebert.krosbridge"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}