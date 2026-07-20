/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * [Dispatchers.Unconfined] runs immediately on the calling thread without its own
 * [kotlinx.coroutines.test.TestCoroutineScheduler], so it never collides with the
 * scheduler `runTest { }` installs for the test body - unlike a fresh
 * `StandardTestDispatcher()`, which would trigger
 * "Detected use of different schedulers" as soon as a use case does
 * `withContext(dispatcherProvider.io()) { ... }`.
 */
fun testDispatcherProvider(dispatcher: CoroutineDispatcher = Dispatchers.Unconfined): DispatcherProvider =
    object : DispatcherProvider {
        override fun io() = dispatcher
        override fun main() = dispatcher
        override fun default() = dispatcher
    }
