@file:Suppress("UnstableApiUsage")
pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://plugins.gradle.org/m2/")
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
  }
}
rootProject.name = "stream-result"
include(":app")
include(":stream-result")
include(":stream-result-call")
include(":stream-result-call-retrofit")