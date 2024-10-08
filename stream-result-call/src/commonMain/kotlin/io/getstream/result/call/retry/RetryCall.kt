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
package io.getstream.result.call.retry

import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.Call.Companion.callCanceledError
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * A wrapper around [Call] that allows retrying the original call based on
 * [RetryPolicy].
 *
 * @param originalCall The original call.
 * @param scope Coroutine scope where the call should be run.
 * @param callRetryService A service responsible for retrying calls based on
 * [RetryPolicy].
 */
internal class RetryCall<T : Any>(
  private val originalCall: Call<T>,
  private val scope: CoroutineScope,
  private val callRetryService: CallRetryService
) : Call<T> {

  private var job: Job? = null
  private val canceled = atomic(false)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    job = scope.launch {
      val result = await()
      withContext(CallDispatcherProvider.Main) {
        yield()
        callback.onResult(result)
      }
    }
  }

  override fun cancel() {
    canceled.value = true
    originalCall.cancel()
    job?.cancel()
  }

  override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
    callRetryService.runAndRetry {
      originalCall
        .takeUnless { canceled.value }
        ?.await()
        ?: callCanceledError()
    }
  }
}
