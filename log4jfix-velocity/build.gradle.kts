plugins {
    id("net.kyori.blossom") version "1.3.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    maven("https://repo.velocitypowered.com/snapshots/")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation(project(":log4jfix-common"))
    compileOnly(group = "", name = "velocity-3.1.1-SNAPSHOT-97")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.0-SNAPSHOT")
}

tasks {
    blossom {
        replaceToken("%%VERSION%%", version, "src/main/java/dev/frankheijden/log4jfix/velocity/Log4jFixVelocity.java")
    }
}

