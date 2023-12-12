plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.10"
}

group = "net.gensokyoreimagined"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}
tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }
}
