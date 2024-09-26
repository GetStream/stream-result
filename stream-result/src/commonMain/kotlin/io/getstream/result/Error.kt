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
package io.getstream.result

import io.getstream.result.Error.NetworkError.Companion.UNKNOWN_STATUS_CODE
import io.getstream.result.internal.StreamHandsOff

/**
 * Represents the generic error model.
 */
public sealed class Error {

  public abstract val message: String

  /**
   * An error that only contains the message.
   *
   * @param message The message describing the error.
   */
  public data class GenericError(override val message: String) : Error()

  /**
   * An error that contains a message and cause.
   *
   * @param message The message describing the error.
   * @param cause The [Throwable] associated with the error.
   */
  public data class ThrowableError(override val message: String, public val cause: Throwable) :
    Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other != null && this::class != other::class) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + cause.hashCode()
    }
  }

  /**
   * An error resulting from the network operation.
   *
   * @param message The message describing the error.
   * @param serverErrorCode The error code returned by the backend.
   * @param statusCode HTTP status code or [UNKNOWN_STATUS_CODE] if not available.
   * @param cause The optional [Throwable] associated with the error.
   */
  public data class NetworkError(
    override val message: String,
    public val serverErrorCode: Int,
    public val statusCode: Int = UNKNOWN_STATUS_CODE,
    public val cause: Throwable? = null
  ) : Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other != null && this::class != other::class) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + (cause?.hashCode() ?: 0)
    }

    public companion object {
      public const val UNKNOWN_STATUS_CODE: Int = -1
    }
  }
}

/**
 * Copies the original [Error] objects with custom message.
 *
 * @param message The message to replace.
 *
 * @return New [Error] instance.
 */
public fun Error.copyWithMessage(message: String): Error {
  return when (this) {
    is Error.GenericError -> this.copy(message = message)
    is Error.NetworkError -> this.copy(message = message)
    is Error.ThrowableError -> this.copy(message = message)
  }
}

/**
 * Extracts the cause from [Error] object or null if it's not available.
 *
 * @return The [Throwable] that is the error's cause or null if not available.
 */
public fun Error.extractCause(): Throwable? {
  return when (this) {
    is Error.GenericError -> null
    is Error.NetworkError -> cause
    is Error.ThrowableError -> cause
  }
}
