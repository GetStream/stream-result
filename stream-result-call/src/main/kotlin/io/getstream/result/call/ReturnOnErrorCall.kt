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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A wrapper around [Call] that swallows the error and emits new data from [onErrorReturn].
 */
public class ReturnOnErrorCall<T : Any>(
  private val originalCall: Call<T>,
  scope: CoroutineScope,
  private val onErrorReturn: suspend (originalError: Error) -> Result<T>
) : Call<T> {

  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch {
      originalCall.enqueue { originalResult ->
        callScope.launch {
          val finalResult = map(originalResult)
          withContext(CallDispatcherProvider.Main) {
            callback.onResult(finalResult)
          }
        }
      }
    }
  }

  override fun cancel() {
    originalCall.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override suspend fun await(): Result<T> = Call.runCatching(::map) {
    withContext(callScope.coroutineContext) {
      map(originalCall.await())
    }
  }

  private suspend fun map(result: Result<T>): Result<T> = when (result) {
    is Result.Success -> result
    is Result.Failure -> onErrorReturn(result.value)
  }
}
