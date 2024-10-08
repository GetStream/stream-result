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

import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class ZipCall<A : Any, B : Any>(
  private val callA: Call<A>,
  private val callB: Call<B>
) : Call<Pair<A, B>> {
  private val canceled = atomic(false)

  override fun cancel() {
    canceled.value = true
    callA.cancel()
    callB.cancel()
  }

  override fun execute(): Result<Pair<A, B>> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<Pair<A, B>>) {
    callA.enqueue { resultA ->
      when {
        canceled.value -> { // no-op
        }

        resultA is Result.Success -> callB.enqueue { resultB ->
          when {
            canceled.value -> null
            resultB is Result.Success -> resultA.combine(resultB)
            resultB is Result.Failure -> getErrorB(resultB)
            else -> null
          }?.let(callback::onResult)
        }

        resultA is Result.Failure -> callback.onResult(getErrorA<A, B>(resultA).also { callB.cancel() })
      }
    }
  }

  private fun <A : Any, B : Any> getErrorA(resultA: Result.Failure): Result<Pair<A, B>> {
    return Result.Failure(resultA.value)
  }

  private fun <A : Any, B : Any> getErrorB(resultB: Result.Failure): Result<Pair<A, B>> {
    return Result.Failure(resultB.value)
  }

  private fun <A : Any, B : Any> Result<A>.combine(result: Result<B>): Result<Pair<A, B>> {
    return if (this is Result.Success && result is Result.Success) {
      Result.Success(Pair(this.value, result.value))
    } else {
      Result.Failure(Error.GenericError("Cannot combine results because one of them failed."))
    }
  }

  override suspend fun await(): Result<Pair<A, B>> = withContext(CallDispatcherProvider.IO) {
    val deferredA = async { callA.await() }
    val deferredB = async { callB.await() }

    val resultA = deferredA.await()
    if (canceled.value) return@withContext Call.callCanceledError()
    if (resultA is Result.Failure) {
      deferredB.cancel()
      return@withContext getErrorA(resultA)
    }

    val resultB = deferredB.await()
    if (canceled.value) return@withContext Call.callCanceledError()
    if (resultB is Result.Failure) {
      return@withContext getErrorB(resultB)
    }

    resultA.combine(resultB)
  }
}
