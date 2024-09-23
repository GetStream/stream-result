/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UnstableApiUsage")

import io.getstream.Configurations

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
  id("de.mannodermaus.android-junit5") version "1.11.0.0"
}

mavenPublishing {
  val artifactId = "stream-result-call"
  coordinates(
    Configurations.artifactGroup,
    artifactId,
    Configurations.versionName
  )

  pom {
    name.set(artifactId)
    description.set("Railway-oriented library to easily model and handle success/failure for Kotlin, Android, and Retrofit.")
  }
}

kotlin {
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
    macosArm64(),
    macosX64(),
  ).forEach {
    it.binaries.framework {
      baseName = "common"
    }
  }

  androidTarget {
    publishLibraryVariants("release")
  }

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    all {
      languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
      languageSettings.optIn("com.skydoves.sandwich.annotations.InternalSandwichApi")
    }
  }

  explicitApi()
}

android {
  compileSdk = Configurations.compileSdk
  namespace = "io.getstream.result.call"
  defaultConfig {
    minSdk = Configurations.minSdk
    consumerProguardFiles("consumer-proguard-rules.pro")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  api(project(":stream-result"))

  implementation(libs.kotlinx.coroutines)
  implementation(libs.stream.log)
  testImplementation(libs.testing.kluent)
  testImplementation(libs.testing.coroutines.test)
  testImplementation(libs.testing.mockito)
  testImplementation(libs.testing.mockito.kotlin)
  testImplementation(libs.testing.mockito.kotlin)
  testImplementation(libs.androidx.test.junit)
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}