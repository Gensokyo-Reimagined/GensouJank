plugins {
  id("mod.base-conventions")
  id("maven-publish")
}

dependencies {
  compileOnly(libs.ignite)
  compileOnly(libs.mixin)

  paperweight.paperDevBundle(libs.versions.paper)
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