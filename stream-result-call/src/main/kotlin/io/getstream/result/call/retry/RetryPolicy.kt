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

import io.getstream.result.Error

/**
 * The retry policy is being used to determine if and when the call should be retried if a temporary error occurred.
 */
public interface RetryPolicy {
  /**
   * Determines whether the call should be retried.
   *
   * @param attempt Current retry attempt.
   * @param error The error returned by the previous attempt.
   *
   * @return true if the call should be retried, false otherwise.
   */
  public fun shouldRetry(attempt: Int, error: Error): Boolean

  /**
   * Provides a timeout used to delay the next call.
   *
   * @param attempt Current retry attempt.
   * @param error The error returned by the previous attempt.
   *
   * @return The timeout in milliseconds before making a retry.
   */
  public fun retryTimeout(attempt: Int, error: Error): Int
}
