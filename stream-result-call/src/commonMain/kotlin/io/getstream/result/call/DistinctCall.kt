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
import io.getstream.result.call.internal.SynchronizedReference
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
public class DistinctCall<T : Any>(
  scope: CoroutineScope,
  private val callBuilder: () -> Call<T>,
  private val onFinished: () -> Unit
) : Call<T> {

  private val distinctScope = scope + SupervisorJob(scope.coroutineContext.job)
  private val deferred = SynchronizedReference<Deferred<Result<T>>>()
  private val delegateCall = atomic<Call<T>?>(initial = null)

  public fun originCall(): Call<T> = callBuilder()

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    distinctScope.launch {
      await().takeUnless { it.isCanceled }?.also { result ->
        withContext(CallDispatcherProvider.Main) {
          callback.onResult(result)
        }
      }
    }
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    deferred.getOrCreate {
      distinctScope.async {
        callBuilder()
          .also { delegateCall.value = it }
          .await()
          .also { doFinally() }
      }
    }.await()
  }

  override fun cancel() {
    delegateCall.value?.cancel()
    distinctScope.coroutineContext.cancelChildren()
    doFinally()
  }

  private fun doFinally() {
    if (deferred.reset()) {
      onFinished()
    }
  }

  private val Result<T>.isCanceled get() = this == Call.callCanceledError<T>()
}
