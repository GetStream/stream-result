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
import io.getstream.result.call.Call.Companion.callCanceledError
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class MapCall<T : Any, K : Any>(
  private val call: Call<T>,
  private val mapper: (T) -> K
) : Call<K> {

  private val canceled = atomic(false)

  override fun cancel() {
    canceled.value = true
    call.cancel()
  }

  override fun execute(): Result<K> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<K>) {
    call.enqueue {
      it.takeUnless { canceled.value }
        ?.map(mapper)
        ?.let(callback::onResult)
    }
  }

  override suspend fun await(): Result<K> = withContext(CallDispatcherProvider.IO) {
    call.await()
      .takeUnless { canceled.value }
      ?.map(mapper)
      .takeUnless { canceled.value }
      ?: callCanceledError()
  }
}
