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

import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
import io.getstream.Configurations

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
  id("de.mannodermaus.android-junit5") version "1.12.2.0"
}

mavenPublishing {
  val artifactId = "stream-result-call-retrofit"
  coordinates(
    Configurations.artifactGroup,
    artifactId,
    Configurations.versionName
  )

  configure(
    AndroidMultiVariantLibrary(
      sourcesJar = true,
      publishJavadocJar = false,
    )
  )

  pom {
    name.set(artifactId)
    description.set("Railway-oriented library to easily model and handle success/failure for Kotlin, Android, and Retrofit.")
  }
}

android {
  namespace = "io.getstream.result.call.retrofit"
  compileSdk = Configurations.compileSdk

  defaultConfig {
    minSdk = Configurations.minSdk
    consumerProguardFiles("consumer-proguard-rules.pro")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

  testImplementation(libs.testing.kluent)
  testImplementation(libs.testing.coroutines.test)
  testImplementation(libs.testing.mockito)
  testImplementation(libs.testing.mockito.kotlin)
  testImplementation(libs.testing.mockito.kotlin)
  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}