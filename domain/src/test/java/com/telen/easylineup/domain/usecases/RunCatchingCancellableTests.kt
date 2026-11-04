/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one behavior [runCatchingCancellable] exists for: unlike plain [runCatching], it must
 * never swallow [CancellationException] into a [Result.failure] - doing so would silently break
 * structured concurrency cancellation for every one of the ~60 usecases that wrap their body
 * with it.
 */
class RunCatchingCancellableTests {
    @Test
    fun wrapsSuccessfulResult() = runTest {
        val result = runCatchingCancellable { 42 }

        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun wrapsOrdinaryThrowableIntoFailure() = runTest {
        val thrown = IllegalStateException("boom")

        val result = runCatchingCancellable<Unit> { throw thrown }

        assertTrue(result.isFailure)
        assertEquals(thrown, result.exceptionOrNull())
    }

    @Test
    fun reThrowsCancellationExceptionInsteadOfWrappingIt() = runTest {
        var caught: CancellationException? = null

        try {
            runCatchingCancellable<Unit> { throw CancellationException("cancelled") }
        } catch (e: CancellationException) {
            caught = e
        }

        assertTrue("CancellationException must propagate, not be wrapped", caught != null)
    }

    @Test
    fun cancellingTheCoroutineWhileInsideTheBlockActuallyCancelsTheJob() = runTest {
        var reachedPastTheSuspensionPoint = false

        val job: Job = async {
            runCatchingCancellable {
                delay(Long.MAX_VALUE)
                reachedPastTheSuspensionPoint = true
            }
        }

        job.cancel()
        job.join()

        assertTrue(job.isCancelled)
        assertFalse(reachedPastTheSuspensionPoint)
    }
}
