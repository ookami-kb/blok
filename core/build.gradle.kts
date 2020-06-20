plugins {
    java
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
}
