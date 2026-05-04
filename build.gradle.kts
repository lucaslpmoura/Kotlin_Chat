#!/usr/bin/env kotlin
import org.gradle.api.publish.maven.MavenPublication

group = "com.github.lucaslpmoura"

plugins {
    kotlin("jvm") version "2.2.0"
    id("java-library")
    id("maven-publish")
}



repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:6.1.0")
    testImplementation("io.kotest:kotest-assertions-core:6.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}


tasks.test {
    useJUnitPlatform()
}

