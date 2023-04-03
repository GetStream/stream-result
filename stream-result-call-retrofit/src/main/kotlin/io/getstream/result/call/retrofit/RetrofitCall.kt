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
package io.getstream.result.call.retrofit

import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.awaitResponse

public class RetrofitCall<T : Any>(
  private val call: retrofit2.Call<T>,
  private val errorParser: ErrorParser<*>,
  scope: CoroutineScope
) : Call<T> {
  private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

  override fun cancel() {
    call.cancel()
    callScope.coroutineContext.cancelChildren()
  }

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    callScope.launch { notifyResult(call.getResult(), callback) }
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    withContext(callScope.coroutineContext) {
      call.getResult()
    }
  }

  private suspend fun notifyResult(result: Result<T>, callback: Call.Callback<T>) =
    withContext(CallDispatcherProvider.Main) {
      callback.onResult(result)
    }

  private fun Throwable.toFailedResult(
    message: String
  ): Result<T> = Result.Failure(this.toFailedError(message))

  private fun Throwable.toFailedError(
    message: String
  ): Error = Error.ThrowableError(cause = this, message = message)

  @Suppress("TooGenericExceptionCaught")
  private suspend fun retrofit2.Call<T>.getResult(): Result<T> =
    withContext(callScope.coroutineContext) {
      try {
        awaitResponse().getResult()
      } catch (t: Throwable) {
        t.toFailedResult(message = t.localizedMessage ?: "retrofit response exception")
      }
    }

  @Suppress("TooGenericExceptionCaught")
  private suspend fun Response<T>.getResult(): Result<T> = withContext(callScope.coroutineContext) {
    if (isSuccessful) {
      try {
        Result.Success(body()!!)
      } catch (t: Throwable) {
        val toFailedResult =
          t.toFailedResult(message = t.localizedMessage ?: "retrofit response exception")
        toFailedResult
      }
    } else {
      val errorBody = errorBody()

      if (errorBody != null) {
        Result.Failure(errorParser.toError(errorBody))
      } else {
        Result.Failure(errorParser.toError(raw()))
      }
    }
  }
}
