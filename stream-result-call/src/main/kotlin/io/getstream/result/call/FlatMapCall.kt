/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.result.flatMapSuspend
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

internal class FlatMapCall<T : Any, K : Any>(
  private val call: Call<T>,
  private val mapper: (T) -> Call<K>
) : Call<K> {
  private val canceled = AtomicBoolean(false)
  private var mappedCall: Call<K>? = null

  override fun cancel() {
    canceled.set(true)
    call.cancel()
    mappedCall?.cancel()
  }

  override fun execute(): Result<K> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<K>) {
    call.enqueue {
      it.takeUnless { canceled.get() }
        ?.onError { callback.onResult(Result.Failure(it)) }
        ?.onSuccess { value ->
          mapper(value)
            .also { mappedCall = it }
            .enqueue {
              it.takeUnless { canceled.get() }?.let(callback::onResult)
            }
        }
    }
  }

  override suspend fun await(): Result<K> =
    call.await()
      .takeUnless { canceled.get() }
      ?.flatMapSuspend {
        mapper(it)
          .also { mappedCall = it }
          .takeUnless { canceled.get() }
          ?.await()
          ?: Call.callCanceledError()
      }
      ?: Call.callCanceledError()
}
