/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import kotlinx.coroutines.CancellationException

/**
 * Like [runCatching] but rethrows [CancellationException] instead of wrapping it into a
 * [Result.failure], so structured concurrency cancellation is never swallowed.
 */
suspend inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        Result.failure(t)
    }
