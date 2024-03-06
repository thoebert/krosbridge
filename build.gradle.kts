import com.github.thoebert.krosbridgecodegen.KROSBridgeCodegenPluginConfig

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

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }


        jvmMain {
            dependencies {
                implementation(libs.ktor.client.java)
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
            }
        }

        commonMain {
            kotlin.srcDirs("${layout.buildDirectory}/generated/source/ros")
            dependencies {
                implementation(libs.ktor.client)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.neogation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
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