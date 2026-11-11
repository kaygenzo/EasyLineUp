/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import androidx.room.withTransaction
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.repository.dao.AppDatabase

/**
 * [TransactionRunner] backed by Room's [withTransaction]: every suspend DAO call made from
 * [block] - even after a `withContext` dispatcher switch - runs on the transaction's own thread,
 * and a nested call joins the outer transaction.
 */
internal class RoomTransactionRunner(private val database: AppDatabase) : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T {
        return database.withTransaction { block() }
    }
}
