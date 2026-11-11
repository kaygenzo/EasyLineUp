/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.ports.TransactionRunner

/**
 * Runs the block directly, but keeps track of the transactions it was asked to open, so a test
 * can check that a use case's writes all happen inside a single one ([isInTransaction] read
 * from a repository mock's answer, [transactions] afterwards), and which ones Room would have
 * rolled back ([rollbacks]: the outermost block ended with an exception). Nested calls join the
 * outer transaction, like Room's.
 */
class TestTransactionRunner : TransactionRunner {
    var transactions = 0
        private set
    var rollbacks = 0
        private set
    private var depth = 0
    val isInTransaction: Boolean
        get() = depth > 0

    override suspend fun <T> inTransaction(block: suspend () -> T): T {
        if (depth == 0) {
            transactions++
        }
        depth++
        try {
            return block()
        } catch (e: Throwable) {
            if (depth == 1) {
                rollbacks++
            }
            throw e
        } finally {
            depth--
        }
    }
}
