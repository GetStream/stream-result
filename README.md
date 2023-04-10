<h1 align="center">Stream Result</h1></br>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/GetStream/stream-result/actions/workflows/android.yml"><img alt="Build Status" src="https://github.com/GetStream/stream-result/actions/workflows/android.yml/badge.svg"/></a>
  <a href="https://androidweekly.net/issues/issue-565"><img alt="Android Weekly" src="https://skydoves.github.io/badges/android-weekly.svg"/></a>
  <a href="https://getstream.io?utm_source=Github&utm_medium=Github_Repo_Content_Ad&utm_content=Developer&utm_campaign=Github_Dec2022_StreamLog&utm_term=DevRelOss"><img src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/HayesGordon/e7f3c4587859c17f3e593fd3ff5b13f4/raw/11d9d9385c9f34374ede25f6471dc743b977a914/badge.json" alt="Stream Feeds"></a>

</p>

<p align="center">
🚊 Railway-oriented library to model and handle success/failure easily for Kotlin, Android, and Retrofit.
</p>

## What's Railway-Oriented Programming?

Railway Oriented Programming is a functional approach to handling success and errors in normalized ways, always allowing you to predict the result. This library helps you to implement Railway-Oriented models and functions in Kotlin and Android (especially with [Retrofit](https://github.com/square/retrofit)). Read [Railway Oriented Programming](https://fsharpforfunandprofit.com/rop/) if you want to learn more about ROP.

<p>
  <a href="https://getstream.io/chat/sdk/android/"><img alt="Logo" src="https://user-images.githubusercontent.com/24237865/229043283-3584b713-42a4-4491-a26c-a06b68b57f0d.jpg"/></a> <br>
</p>

<a href="https://getstream.io/chat/sdk/compose?utm_source=Github&utm_medium=Github_Repo_Content_Ad&utm_content=Developer&utm_campaign=Github_Dec2022_Jaewoong_ChatGPT&utm_term=DevRelOss">
<img src="https://user-images.githubusercontent.com/24237865/138428440-b92e5fb7-89f8-41aa-96b1-71a5486c5849.png" align="right" width="12%"/>
</a>

## Use Cases

You'll find the use cases in the repositories below:
- [Stream Chat SDK for Android](https://github.com/getStream/stream-chat-android): 💬 Android Chat SDK ➜ Stream Chat API. UI component libraries for chat apps. Kotlin & Jetpack Compose messaging SDK for Android chat.
- [Stream Video SDK for Android](https://getstream.io/video/): Coming soon!

<img align="right" width="90px" src="https://user-images.githubusercontent.com/24237865/178630165-76855349-ac04-4474-8bcf-8eb5f8c41095.png"/>

## Stream Result

This library provides a normalized result model, `Result`, representing the success or error of any business work.

[![Maven Central](https://img.shields.io/maven-central/v/io.getstream/stream-result.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.getstream%22%20AND%20a:%22stream-result%22)
<br>

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation("io.getstream:stream-result:$version")
}
```

## Result

This is a basic model to represent a normalized result from business work. This looks similar to [Kotlin's Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/), but Stream Result was designed to include more information about success and error and support more convenient functionalities to handle results. Result is basically consist of two detailed types below:

- **Result.Success**: This represents your business's successful result, including a `value` property, a generic type of Result.
- **Result.Failure**: This represents the failed result of your business result and includes a `value` property, the `Error` type.

You can simply create each instance of `Result` like the example below:

```kotlin
val result0: Result<String> = Result.Success(value = "result")

val result1: Result<String> = Result.Failure(
  value = Error.GenericError(message = "failure")
)

val result = result0 then { result1 }
result.onSuccess {
  ..
}.onError {
  ..
}
```

## Error

`Result.Failure` has `Error` as a value property, which contains error details of your business work. Basically, `Error` consists of three different types of errors below:

- **Error.GenericError**: Represents a normal type of error and only contains an error message.
- **Error.ThrowableError**: Represents an exceptional type of error and contains a message and cause information.
- **Error.NetworkError**: Represents a network error and contains status code, message, and cause information.

You can create each instance like the example below:

```kotlin
val error: Error = Error.GenericError(message = "error")

try {
     .. 
} catch (e: Exception) {
  val error: Error = Error.ThrowableError(
    message = e.localizedMessage ?: e.stackTraceToString(), 
    cause = e
  )
}

val error: Error = Error.NetworkError(
  message = "error",
  serverErrorCode = code,
  statusCode = statusCode
)
```

## Result Extensions

**Stream Result** library useful extensions below to effectively achieve Railway Oriented Programming in Kotlin: 

### Result.then

Composition the `Result` with a given `Result` from a lambda function.

```kotlin
val result0: Result<String> = Result.Success(value = "result0")
val result1: Result<Int> = Result.Success(value = 123)
val result = result0 then { result1 }
result.onSuccess { intValue -> .. }
```

### Result.map, Result.mapSuspend

Returns a transformed `Result` of applying the given function if the `Result` contains a successful data payload.

```kotlin
val result: Result<String> = Result.Success(value = "result")
val mappedResult = result.map { 123 }
mappedResult.onSuccess { intValue -> }
```

### Result.flatMap, Result.flatMapSuspend

Returns a transformed `Result` from results of the function if the `Result` contains a successful data payload. Returns an original `Result` if the `Result` contains an error payload.

```kotlin
val result: Result<String> = Result.Success(value = "result")
val mappedResult = result.flatMap { Result.Success(value = 123) }
mappedResult.onSuccess { intValue -> }
```

<img align="right" width="140px" src="https://user-images.githubusercontent.com/24237865/205479526-5fa0b5f0-22df-4f02-ac0e-7a7a3e050cdb.png"/>

## Stream Result Call Retrofit

**Stream Result** library provides retrofit call integration functionalities to help you to construct a `Result` model easily from the network requests on Android with the same approaches of Railway Oriented Programming.

[![Maven Central](https://img.shields.io/maven-central/v/io.getstream/stream-result.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.getstream%22%20AND%20a:%22stream-result%22)

Add the dependency below into your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation("io.getstream:stream-result-call-retrofit:$version")
}
```

### RetrofitCallAdapterFactory and RetrofitCall

You can return `RetrofitCall` class as a return type on your Retrofit services by adding `RetrofitCallAdapterFactory` on your `Retrofit.Builder` like the example below:

```kotlin
val retrofit: Retrofit = Retrofit.Builder()
  .baseUrl(..)
  .addCallAdapterFactory(RetrofitCallAdapterFactory.create())
  .build()

val posterService: PosterService = retrofit.create()

interface PosterService {

  @GET("DisneyPosters2.json")
  fun fetchPosterList(): RetrofitCall<List<Poster>>
}
```

`RetrofitCall` class allows you to execute network requests easily like the example below:

```kotlin
interface PosterService {

  @GET("DisneyPosters2.json")
  fun fetchPosterList(): RetrofitCall<List<Poster>>
}

val posterService: PosterService = retrofit.create()

// Execute a network request asynchronously with a given callback.
posterService.fetchPosterList().enqueue { posters ->
  ..
}

// Execute a network request in a coroutine scope.
// If you use coroutines, we'd recommend you to use this way.
viewModelScope.launch {
  val result = posterService.fetchPosterList().await()
  result.onSuccess {
    ..
  }.onError {
    ..
  }
}
```

### RetrofitCall Extensions

`RetrofitCall` provides useful extensions for sequential works following Railway Oriented Programming approaches.

#### RetrofitCall.doOnStart

Run the given `function` before running a network request.

```kotlin
val result = posterService.fetchPosterList()
  .doOnStart(viewModelScope) {
    // do something..
  }.await()
```

#### RetrofitCall.doOnResult

Run the given `function` before running a network request.

```kotlin
val result = posterService.fetchPosterList()
  .doOnStart(viewModelScope) {
    // do something before running the call..
  }
  .doOnResult(viewModelScope) {
    // do something after running the call..
  }.await()
```

#### RetrofitCall.map

Maps a `Call` type to a transformed `Call`.

```kotlin
val result = posterService.fetchPosterList()
  .map { it.first() }
  .await()
```

So you can chain all the extensions sequentially like the example below:

```kotlin
val result = posterService.fetchPosterList()
  // retry if the network request fails.
  .retry(viewModelScope, retryPolicy)
  // do something before running the network request.
  .doOnStart(viewModelScope) {
    // do something..
  }
  // do something after running the network request.
  .doOnResult(viewModelScope) {
    // do something..
  }
  // map the type of call.
  .map { it.first() }
  .await()

result.onSuccess {
  // do something..
}.onError {
  // do something..
}
```

#### RetrofitCall.retry

Retry a network request following your `RetryPolicy`.

```kotlin
private val retryPolicy = object : RetryPolicy {
  override fun shouldRetry(attempt: Int, error: Error): Boolean = attempt <= 3

  override fun retryTimeout(attempt: Int, error: Error): Int = 3000
}


val result = posterService.fetchPosterList()
  // retry if the network request fails.
  .retry(viewModelScope, retryPolicy)
  .await()
```

### Custom Error Parser

You can customize the creating of `Error` from an error response according to your backend service by implementing your `ErrorParser` class. You can provide your custom `ErrorParser` to `RetrofitCallAdapterFactory`. If not, it will use a default `ErrorParser`, which uses [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html) to decode json formats.

```kotlin
internal class MyErrorParser : ErrorParser<DefaultErrorResponse> {

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> fromJson(raw: String): T {
    // use moshi or something that you can serialize from json response.
  }

  override fun toError(okHttpResponse: Response): Error {
    // build Error with a given okHttpResponse.
  }

  override fun toError(errorResponseBody: ResponseBody): Error {
    // build Error with a given errorResponseBody.
  }
}

val retrofit: Retrofit = Retrofit.Builder()
  .baseUrl(..)
  .addConverterFactory(MoshiConverterFactory.create())
  .addCallAdapterFactory(RetrofitCallAdapterFactory.create(
    errorParser = MyErrorParser()
  ))
  .build()

```

<a href="https://getstream.io/chat/compose/tutorial/?utm_source=Github&utm_campaign=Devrel_oss&utm_medium=StreamResult"><img src="https://user-images.githubusercontent.com/24237865/146505581-a79e8f7d-6eda-4611-b41a-d60f0189e7d4.jpeg" align="right" /></a>

## Find this repository useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/retrofit-adapters/stargazers)__ for this repository. :star: <br>
Also, __[contributors](https://github.com/skydoves)__ on GitHub for my next creations! 🤩

# License
```xml
Copyright 2023 Stream.IO, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
