
rootProject.name = "krosbridge"


pluginManagement {
    repositories{
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}