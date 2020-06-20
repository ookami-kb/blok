plugins {
    java
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

repositories {
    mavenCentral()
}

allprojects {
    group = "com.github.ookami-kb"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "maven-publish")
}
