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
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.ksp.get().pluginId)
}

android {
  namespace = "io.getstream.resultdemo"
  compileSdk = Configurations.compileSdk

  defaultConfig {
    applicationId = "io.getstream.resultdemo"
    minSdk = Configurations.minSdk
    targetSdk = Configurations.targetSdk
    versionName = Configurations.versionName
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  lint {
    abortOnError = false
  }
}

dependencies {
  // result
  implementation(project(":stream-result"))
  implementation(project(":stream-result-call"))
  implementation(project(":stream-result-call-retrofit"))

  // androidx
  implementation(libs.androidx.material)
  implementation(libs.androidx.viewmodel.ktx)

  // network & coroutines
  implementation(libs.retrofit)
  implementation(libs.retrofit.moshi)
  implementation(libs.okhttp.logging)
  implementation(libs.kotlinx.coroutines)

  // moshi
  implementation(libs.moshi)
  ksp(libs.moshi.codegen)

  // logger
  implementation(libs.stream.log)
}
