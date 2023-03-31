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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

public class RetrofitCallAdapterFactory private constructor(
  private val coroutineScope: CoroutineScope,
  private val errorParser: ErrorParser<*>
) : CallAdapter.Factory() {

  override fun get(
    returnType: Type,
    annotations: Array<out Annotation>,
    retrofit: Retrofit
  ): CallAdapter<*, *>? {
    if (getRawType(returnType) != RetrofitCall::class.java) {
      return null
    }
    if (returnType !is ParameterizedType) {
      throw IllegalArgumentException("Call return type must be parameterized as Call<Foo>")
    }
    val responseType: Type = getParameterUpperBound(0, returnType)
    return RetrofitCallAdapter<Any>(responseType, coroutineScope, errorParser)
  }

  public companion object {
    public fun create(
      coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
      errorParser: ErrorParser<*> = DefaultErrorParser()
    ): RetrofitCallAdapterFactory = RetrofitCallAdapterFactory(coroutineScope, errorParser)
  }
}
