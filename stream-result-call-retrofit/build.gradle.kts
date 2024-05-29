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

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
}

rootProject.extra.apply {
  set("PUBLISH_GROUP_ID", Configurations.artifactGroup)
  set("PUBLISH_ARTIFACT_ID", "stream-result-call-retrofit")
  set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from ="${rootDir}/scripts/publish-module.gradle")

android {
  namespace = "io.getstream.result.call.retrofit"
  compileSdk = Configurations.compileSdk

  defaultConfig {
    minSdk = Configurations.minSdk
    consumerProguardFiles("consumer-proguard-rules.pro")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.freeCompilerArgs += listOf(
    "-Xexplicit-api=strict"
  )
}

dependencies {
  api(project(":stream-result-call"))

  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.stream.log)
}