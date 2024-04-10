pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvnrepository.com/artifact/space.vectrix.ignite/ignite-api")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "GensouJankCore"
include("GensouJank")
include("GensouJankMod")
