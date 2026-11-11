/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports

/**
 * Runs several repository writes as one database transaction: either all of them land, or, if
 * [block] throws (including a coroutine cancellation), none of them do. Readers observing the
 * database also see a single change once [block] returns, instead of every intermediate write.
 *
 * Calls nest: a use case running inside another one's transaction joins it rather than opening
 * a second one.
 */
interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T
}
