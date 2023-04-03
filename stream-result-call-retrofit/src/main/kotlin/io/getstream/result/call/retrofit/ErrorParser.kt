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
import okhttp3.Response
import okhttp3.ResponseBody

public interface ErrorParser<T> {

  public fun <T : Any> fromJson(raw: String): T

  public fun toError(okHttpResponse: Response): Error {
    val statusCode: Int = okHttpResponse.code

    return try {
      // Try to parse default Stream error body
      val body = okHttpResponse.peekBody(Long.MAX_VALUE).string()

      if (body.isEmpty()) {
        Error.NetworkError(
          message = okHttpResponse.message,
          serverErrorCode = statusCode,
          statusCode = statusCode
        )
      } else {
        Error.NetworkError(
          message = okHttpResponse.message,
          serverErrorCode = statusCode,
          statusCode = statusCode
        )
      }
    } catch (expected: Throwable) {
      Error.ThrowableError(
        message = expected.message ?: expected.stackTraceToString(),
        cause = expected
      )
    }
  }

  public fun toError(errorResponseBody: ResponseBody): Error {
    return try {
      val errorResponse: DefaultErrorResponse = fromJson(errorResponseBody.string())
      val (code, message, statusCode) = errorResponse

      Error.NetworkError(
        serverErrorCode = code,
        message = message,
        statusCode = statusCode
      )
    } catch (expected: Throwable) {
      Error.ThrowableError(
        message = expected.message ?: expected.stackTraceToString(),
        cause = expected
      )
    }
  }
}
