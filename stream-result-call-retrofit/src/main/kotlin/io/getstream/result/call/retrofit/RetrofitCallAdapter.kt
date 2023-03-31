/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class RetrofitCallAdapter<T : Any>(
  private val responseType: Type,
  private val coroutineScope: CoroutineScope,
  private val errorParser: ErrorParser<*>
) : CallAdapter<T, Call<T>> {
  override fun responseType(): Type = responseType
  override fun adapt(call: retrofit2.Call<T>): Call<T> {
    return RetrofitCall(call, errorParser, coroutineScope)
  }
}
