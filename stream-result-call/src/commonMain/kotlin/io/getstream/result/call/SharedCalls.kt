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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Creates a shared [Call] instance.
 */
@Suppress("FunctionName", "UNCHECKED_CAST")
internal fun <T : Any> SharedCall(
  origin: Call<T>,
  originIdentifier: () -> Int,
  scope: CoroutineScope
): Call<T> {
  val sharedCalls = scope.coroutineContext[SharedCalls] ?: return origin
  val identifier = originIdentifier()
  return sharedCalls[identifier] as? Call<T>
    ?: DistinctCall(scope, { origin }) {
      sharedCalls.remove(identifier)
    }.also {
      sharedCalls.put(identifier, it)
    }
}

/**
 * The [CoroutineContext.Element] which holds ongoing calls until those get finished.
 *
 * The purpose of shared calls is to stop side effects for the same call executing multiple times.
 */
public class SharedCalls : CoroutineContext.Element {

  /**
   * A key of [SharedCalls] coroutine context element.
   */
  public override val key: CoroutineContext.Key<SharedCalls> = Key

  /**
   * A collection of uncompleted calls.
   */
  private val calls = atomic<MutableMap<Int, Call<out Any>>>(initial = mutableMapOf())

  /**
   * Provides a [Call] based of specified [identifier] if available.
   */
  internal operator fun get(identifier: Int): Call<out Any>? {
    return calls.value[identifier]
  }

  /**
   * Puts a [Call] behind of specified [identifier].
   */
  internal fun put(identifier: Int, value: Call<out Any>) {
    calls.value[identifier] = value
  }

  /**
   * Removes a [Call] based of specified [identifier].
   */
  internal fun remove(identifier: Int) {
    calls.value.remove(identifier)
  }

  public companion object Key : CoroutineContext.Key<SharedCalls>
}
