plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.5.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":log4jfix-common"))
    compileOnly("net.md-5:bungeecord-proxy:1.18-R0.1-SNAPSHOT")
}

bungee {
    name = "Log4jFix"
    main = (group as String) + ".Log4jFixBungee"
    author = "FrankHeijden"
}
