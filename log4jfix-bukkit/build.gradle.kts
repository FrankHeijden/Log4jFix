plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    implementation(project(":log4jfix-common"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.4.0")
}

bukkit {
    name = "Log4jFix"
    main = (group as String) + ".Log4jFixBukkit"
    apiVersion = "1.13"
    authors = listOf("FrankHeijden")
    depend = listOf("ProtocolLib")
}
