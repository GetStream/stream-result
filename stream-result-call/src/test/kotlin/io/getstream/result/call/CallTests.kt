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
import io.getstream.result.call.retry.RetryPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class CallTests {

  companion object {
    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()
  }

  @Test
  fun `Should invoke methods in right order`() = runTest {
    val mutableList = mutableListOf<Int>()

    CoroutineCall(testCoroutines.scope) {
      mutableList.add(2)
      Result.Success(2)
    }
      .doOnStart(testCoroutines.scope) { mutableList.add(1) }
      .doOnResult(testCoroutines.scope) { mutableList.add(4) }
      .enqueue {
        mutableList.add(3)
      }

    mutableList shouldBeEqualTo listOf(1, 2, 3, 4)
  }

  @Test
  fun `Should return from onErrorReturn when original call gives error`() = runTest {
    val result = CoroutineCall<List<Int>>(testCoroutines.scope) {
      Result.Failure(Error.GenericError(message = "Test error"))
    }.onErrorReturn(testCoroutines.scope) {
      Result.Success(listOf(0, 1))
    }.await()
    result shouldBeEqualTo Result.Success(listOf(0, 1))
  }

  @Test
  fun `Should return from onErrorReturn when precondition fails`() = runTest {
    val result = CoroutineCall(testCoroutines.scope) {
      Result.Success(listOf(10, 20, 30))
    }.withPrecondition(testCoroutines.scope) {
      Result.Failure(Error.GenericError(message = "Error from precondition"))
    }.onErrorReturn(testCoroutines.scope) {
      Result.Success(listOf(0, 1))
    }.await()
    result shouldBeEqualTo Result.Success(listOf(0, 1))
  }

  @Test
  fun `Should not return from onErrorReturn when original call gives success`() = runTest {
    val result = CoroutineCall(testCoroutines.scope) {
      Result.Success(listOf(10, 20, 30))
    }.onErrorReturn(testCoroutines.scope) {
      Result.Success(listOf(0, 1))
    }.await()
    result shouldBeEqualTo Result.Success(listOf(10, 20, 30))
  }

  @Test
  fun `Should retry a call according to RetryPolicy`() = runTest {
    var currentValue = 0
    val maxAttempts = 3
    val retryPolicy = object : RetryPolicy {
      override fun shouldRetry(attempt: Int, error: Error): Boolean = attempt < maxAttempts

      override fun retryTimeout(attempt: Int, error: Error): Int = 0
    }

    CoroutineCall(testCoroutines.scope) {
      currentValue++
      Result.Failure(Error.GenericError(message = ""))
    }
      .retry(testCoroutines.scope, retryPolicy)
      .doOnStart(testCoroutines.scope) { currentValue++ }
      .enqueue {
        currentValue++
      }

    currentValue `should be equal to` maxAttempts + 2
  }
}
