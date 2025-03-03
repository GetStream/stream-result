/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.getstream.result.call.retrofit

import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

public class TestCoroutineExtension :
  BeforeEachCallback,
  BeforeAllCallback,
  AfterEachCallback,
  AfterAllCallback {

  private var _scope: TestScope? = null
  public val dispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
  public val scope: TestScope
    get() = requireNotNull(_scope)
  private var beforeAllCalled: Boolean = false

  override fun beforeAll(context: ExtensionContext) {
    Dispatchers.setMain(dispatcher)
    CallDispatcherProvider.set(
      mainDispatcher = dispatcher,
      ioDispatcher = dispatcher
    )
    beforeAllCalled = true
  }

  override fun afterEach(context: ExtensionContext) {
    check(beforeAllCalled) { "TestCoroutineExtension field must be static" }
  }

  override fun afterAll(context: ExtensionContext) {
    Dispatchers.resetMain()
    CallDispatcherProvider.reset()
    _scope = null
  }

  override fun beforeEach(context: ExtensionContext?) {
    _scope = TestScope(dispatcher)
  }
}
