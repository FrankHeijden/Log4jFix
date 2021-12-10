plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "dev.frankheijden.log4jfix"
version = "1.0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":log4jfix-common", "shadow"))
    implementation(project(":log4jfix-bukkit", "shadow"))
    implementation(project(":log4jfix-bungee", "shadow"))
    implementation(project(":log4jfix-velocity", "shadow"))
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = (rootProject.group as String) + "." + project.name.substringAfter("log4jfix-")
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}
