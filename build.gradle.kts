@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.nexus.plugin) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinBinaryCompatibilityValidator)
}

apiValidation {
  ignoredProjects.addAll(listOf("app"))
  nonPublicMarkers.add("kotlin.PublishedApi")
}

subprojects {
  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
  }

  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("${layout.buildDirectory}/**/*.kt")
      ktlint().setUseExperimental(true).editorConfigOverride(
        mapOf(
          "indent_size" to "2",
          "continuation_indent_size" to "2"
        )
      )
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
      trimTrailingWhitespace()
      endWithNewline()
    }
    format("kts") {
      target("**/*.kts")
      targetExclude("${layout.buildDirectory}/**/*.kts")
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
      target("**/*.xml")
      targetExclude("**/build/**/*.xml")
      licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
    }
  }
}