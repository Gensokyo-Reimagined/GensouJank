plugins {
    id("mod.base-conventions")
    id("maven-publish")
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

configurations {
    create("conf")
}

var jarFile = file("build/libs/%s-%s-dev-all.jar".format(project.name, project.version))
var jarArtifact = artifacts.add("conf", jarFile) {
    type = "jar"
    builtBy("jar")
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(jarArtifact)
            group = "plugins"
        }
    }

    repositories {
        maven {
            name = "gensorepo"
            credentials {
                username = (project.findProperty("gpr.user") as String?) ?: System.getenv("USERNAME")
                password = (project.findProperty("gpr.key") as String?) ?: System.getenv("TOKEN")
            }
            // url to the releases maven repository
            url = uri("https://repo.gensokyoreimagined.net/")
        }
    }
}