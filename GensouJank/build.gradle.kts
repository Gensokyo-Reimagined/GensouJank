plugins {
    id("mod.base-conventions")
}

group = "net.gensokyoreimagined"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)
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
