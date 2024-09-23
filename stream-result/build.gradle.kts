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
}

mavenPublishing {
  val artifactId = "stream-result"
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
    }
    val commonMain by getting {
      dependencies {
      }
    }
  }

  explicitApi()
}

android {
  compileSdk = Configurations.compileSdk
  namespace = "io.getstream.result"
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
}