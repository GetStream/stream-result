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
package io.getstream.result.call

import io.getstream.result.Result
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class BlockedTask<T : Any>(private val result: Result<T>) {

  private val isBlocked = AtomicBoolean(true)
  private val started = AtomicBoolean(false)
  private val completed = AtomicBoolean(false)

  fun unblock() {
    isBlocked.set(false)
  }

  fun getSyncTask(): () -> Result<T> = { runBlocking { awaitResult() } }

  fun getSuspendTask(): suspend CoroutineScope.() -> Result<T> = { awaitResult() }

  private suspend fun awaitResult() = withContext(CallDispatcherProvider.IO) {
    started.set(true)
    while (isBlocked.get()) {
      delay(10)
    }
    completed.set(true)
    result
  }

  fun isStarted(): Boolean = started.get()
  fun isCompleted(): Boolean = completed.get()
}
