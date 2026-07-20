/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * [Dispatchers.Unconfined] runs immediately on the calling thread without its own
 * [kotlinx.coroutines.test.TestCoroutineScheduler], so it never collides with the
 * scheduler `runTest { }` installs for the test body.
 */
fun testDispatcherProvider(dispatcher: CoroutineDispatcher = Dispatchers.Unconfined): DispatcherProvider =
    object : DispatcherProvider {
        override fun io() = dispatcher
        override fun main() = dispatcher
        override fun default() = dispatcher
    }
