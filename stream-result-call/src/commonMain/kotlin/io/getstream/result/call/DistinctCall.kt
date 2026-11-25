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

import io.getstream.result.Result
import io.getstream.result.call.dispatcher.CallDispatcherProvider
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
public class DistinctCall<T : Any>(
  private val scope: CoroutineScope,
  private val callBuilder: () -> Call<T>,
  private val onFinished: () -> Unit
) : Call<T> {

  private val distinctScope = atomic(newDistinctScope())
  private val deferred = atomic<Deferred<Result<T>>?>(initial = null)
  private val delegateCall = atomic<Call<T>?>(initial = null)

  public fun originCall(): Call<T> = callBuilder()

  override fun execute(): Result<T> = runBlocking { await() }

  override fun enqueue(callback: Call.Callback<T>) {
    liveScope().launch {
      await().takeUnless { it.isCanceled }?.also { result ->
        withContext(CallDispatcherProvider.Main) {
          callback.onResult(result)
        }
      }
    }
  }

  override suspend fun await(): Result<T> = Call.runCatching {
    currentOrStartExecution().await()
  }

  override fun cancel() {
    delegateCall.value?.cancel()
    distinctScope.value.coroutineContext.cancelChildren()
    deferred.value?.let(::finish)
  }

  private fun currentOrStartExecution(): Deferred<Result<T>> {
    while (true) {
      val current = deferred.value
      if (current != null && !current.isCancelled) return current
      val execution = liveScope().async(start = CoroutineStart.LAZY) {
        callBuilder()
          .also { delegateCall.value = it }
          .await()
          .also { finish(coroutineContext.job) }
      }
      // A still-cancelling execution would only hand this subscriber a cancellation, so it's replaced without
      // calling onFinished: the call keeps running and must stay registered.
      if (deferred.compareAndSet(current, execution)) {
        // Cancelling the parent scope ends the execution without reaching the finish() above.
        execution.invokeOnCompletion { finish(execution) }
        execution.start()
        return execution
      }
      execution.cancel()
    }
  }

  /**
   * Clears [execution] and notifies [onFinished] if it is still the current one. A replaced execution never notifies.
   */
  private fun finish(execution: Job) {
    val current = deferred.value
    if (current === execution && deferred.compareAndSet(current, null)) {
      onFinished()
    }
  }

  /**
   * A cancelled parent scope cancels the distinct scope for good, so later subscribers get a fresh one.
   */
  private fun liveScope(): CoroutineScope {
    val current = distinctScope.value
    if (current.isActive || !scope.isActive) return current
    val fresh = newDistinctScope()
    if (distinctScope.compareAndSet(current, fresh)) return fresh
    // Detaches the losing job from the parent.
    fresh.cancel()
    return distinctScope.value
  }

  private fun newDistinctScope(): CoroutineScope = scope + SupervisorJob(scope.coroutineContext.job)

  private val Result<T>.isCanceled get() = this == Call.callCanceledError<T>()
}
