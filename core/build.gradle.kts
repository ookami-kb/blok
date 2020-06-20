plugins {
    java
    kotlin("jvm")
    maven
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    testCompile("junit", "junit", "4.12")
}
