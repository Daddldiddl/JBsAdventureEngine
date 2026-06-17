plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    kotlin("plugin.compose") version "2.4.0"
    id("org.jetbrains.compose") version "1.6.11"
}

import org.gradle.api.tasks.JavaExec

group = "net.daddldiddl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.windows_x64)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    runtimeOnly("org.jetbrains.skiko:skiko-awt-runtime-windows-arm64:0.8.4")

    // Fast MVP integration strategy: consume local model-lib output JAR.
    implementation(files("../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.register("verifyModelLibJar") {
    group = "verification"
    description = "Warns if the local model-lib JAR expected by the editor is missing."

    doLast {
        val modelJar = file("../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar")
        if (!modelJar.exists()) {
            logger.warn("model-lib JAR not found at: ${modelJar.absolutePath}")
            logger.warn("Run from repo root: mvn -DskipTests package")
        } else {
            logger.lifecycle("Found model-lib JAR: ${modelJar.absolutePath}")
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.daddldiddl.jbsadventure.editor.MainKt"

        nativeDistributions {
            packageName = "jbs-adventure-editor"
            packageVersion = "1.0.0"
        }
    }
}
