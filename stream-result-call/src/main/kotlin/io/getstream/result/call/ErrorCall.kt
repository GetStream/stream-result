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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ErrorCall<T : Any>(
  private val scope: CoroutineScope,
  private val e: Error
) : Call<T> {
  override fun cancel() {
    // Not supported
  }

  override fun execute(): Result<T> {
    return Result.Failure(e)
  }

  override fun enqueue(callback: Call.Callback<T>) {
    scope.launch(CallDispatcherProvider.Main) {
      callback.onResult(Result.Failure(e))
    }
  }

  override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
    Result.Failure(e)
  }
}
