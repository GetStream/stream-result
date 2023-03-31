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
package io.getstream.resultdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.log.StreamLog
import io.getstream.log.streamLog
import io.getstream.result.StreamError
import io.getstream.result.call.doOnResult
import io.getstream.result.call.doOnStart
import io.getstream.result.call.map
import io.getstream.result.call.retry
import io.getstream.result.call.retry.RetryPolicy
import kotlinx.coroutines.launch

public class MainViewModel constructor(
  private val posterService: PosterService
) : ViewModel() {

  fun fetchPosterList() {
    viewModelScope.launch {
      val result = posterService.fetchPosterList()
        // retry if the network request fails.
        .retry(viewModelScope, retryPolicy)
        // do something before running the network request.
        .doOnStart(viewModelScope) {
          StreamLog.streamLog { "doOnStart" }
        }
        // do something after running the network request.
        .doOnResult(viewModelScope) {
          StreamLog.streamLog { "doOnResult" }
        }
        // map the type of call.
        .map {
          StreamLog.streamLog { "map" }
          it.first()
        }
        .await()

      result.onSuccess {
        StreamLog.streamLog { "onSuccess: $it" }
      }.onError {
        StreamLog.streamLog { "onError: $it" }
      }
    }
  }

  private val retryPolicy = object : RetryPolicy {
    override fun shouldRetry(attempt: Int, error: StreamError): Boolean = attempt <= 3

    override fun retryTimeout(attempt: Int, error: StreamError): Int = 3000
  }
}
