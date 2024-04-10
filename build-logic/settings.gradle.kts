rootProject.name = "ignite-build-logic"

dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvnrepository.com/artifact/space.vectrix.ignite/ignite-api")
  }

  versionCatalogs {
    register("libs") {
      from(files("../gradle/libs.versions.toml")) // include from parent project
    }
  }
}
