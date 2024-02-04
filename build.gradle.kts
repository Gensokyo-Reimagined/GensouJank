plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.gensokyoreimagined"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    implementation("net.bytebuddy:byte-buddy:1.14.11")
}
tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }
    shadowJar {

        relocate("net.bytebuddy", "net.gensokyoreimagined.net.dependencies.net.bytebuddy")

        manifest {
            attributes["Agent-Class"] = "net.gensokyoreimagined.gensoujank.ServerAgent"
            attributes["Can-Redefine-Classes"] = true
            attributes["Premain-Class"] = "net.gensokyoreimagined.gensoujank.ServerAgent"
            attributes["Can-Retransform-Classes"] = true
        }
    }
}
