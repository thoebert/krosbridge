import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.thoebert.krosbridgecodegen.KROSBridgeCodegenPluginConfig

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
    id("io.github.thoebert.krosbridge-codegen") version "1.0.1"
}

group = "com.github.thoebert"
version = "1.0"

repositories {
    mavenCentral()
}

val ktor_version: String by project
dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    testImplementation("io.ktor:ktor-client-okhttp:$ktor_version")
    testImplementation("org.glassfish:jakarta.json:2.0.1")
    testImplementation("org.slf4j:jul-to-slf4j:1.7.36")
    testImplementation("org.apache.logging.log4j:log4j-core:2.17.1")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    testImplementation("org.glassfish.tyrus:tyrus-server:1.17")
    testImplementation("org.glassfish.tyrus:tyrus-container-grizzly-server:1.17")
    testImplementation(kotlin("test"))
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.4") // YAML Config
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1") // YAML Config
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


configure<KROSBridgeCodegenPluginConfig> {
    packageName.set("com.github.thoebert.krosbridge.messages")
}

tasks.jar {
    dependsOn("generateROSSources")
}