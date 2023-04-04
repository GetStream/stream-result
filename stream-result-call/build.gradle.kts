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
  kotlin("jvm")
}

rootProject.extra.apply {
  set("PUBLISH_GROUP_ID", Configurations.artifactGroup)
  set("PUBLISH_ARTIFACT_ID", "stream-result-call")
  set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from ="${rootDir}/scripts/publish-module.gradle")

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.freeCompilerArgs += listOf(
    "-Xexplicit-api=strict"
  )
}

testing {
  suites {
    // Using JVM Test Suite feature to configure our test task.
    val test by getting(JvmTestSuite::class) {
      // For JUnit 5 we need to enable JUnit Jupiter.
      // If we don't specify a version the default
      // version is used, which is 5.8.2 with Gradle 7.6.
      // We can use a version as String as argument, but it is even
      // better to refer to a version from the version catalog,
      // so all versions for our dependencies are at the
      // single location of the version catalog.
      // We define the version in libs.versions.toml.
      useJUnitJupiter(libs.versions.junit5)
    }
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
}