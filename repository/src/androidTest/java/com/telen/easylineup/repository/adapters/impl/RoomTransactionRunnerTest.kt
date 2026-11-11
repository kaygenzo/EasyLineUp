/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.repository.dao.AppDatabase
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.RoomTournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Checks, on a real database, the guarantee use cases rely on: a write sequence run through
 * [RoomTransactionRunner] lands entirely or not at all - including when the use case switches to
 * its IO dispatcher inside the block, and when one use case nests another's transaction.
 */
@RunWith(AndroidJUnit4::class)
class RoomTransactionRunnerTest {
    private lateinit var db: AppDatabase
    private lateinit var runner: RoomTransactionRunner

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        runner = RoomTransactionRunner(db)
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    @Test
    fun keepsEveryWriteWhenTheBlockSucceeds() = runBlocking {
        runner.inTransaction {
            db.teamDao().insertTeam(RoomTeam(name = "Riverside Hawks"))
            db.tournamentDao().insertTournament(RoomTournament(name = "Fall Classic"))
        }

        assertEquals(1, db.teamDao().getTeams().size)
        assertEquals(1, db.tournamentDao().getTournaments().size)
    }

    @Test
    fun rollsEveryWriteBackWhenTheBlockThrows() = runBlocking {
        try {
            runner.inTransaction {
                db.teamDao().insertTeam(RoomTeam(name = "Riverside Hawks"))
                db.tournamentDao().insertTournament(RoomTournament(name = "Fall Classic"))
                throw IllegalStateException("write failed halfway")
            }
            fail("The block's exception should reach the caller")
        } catch (expected: IllegalStateException) {
            // expected
        }

        assertTrue(db.teamDao().getTeams().isEmpty())
        assertTrue(db.tournamentDao().getTournaments().isEmpty())
    }

    @Test
    fun rollsBackTheOuterWritesWhenANestedTransactionOnIoThrows() = runBlocking {
        try {
            runner.inTransaction {
                withContext(Dispatchers.IO) {
                    db.teamDao().insertTeam(RoomTeam(name = "Riverside Hawks"))
                    runner.inTransaction {
                        withContext(Dispatchers.IO) {
                            db.tournamentDao().insertTournament(RoomTournament(name = "Cup"))
                            throw IllegalStateException("nested write failed")
                        }
                    }
                }
            }
            fail("The nested block's exception should reach the caller")
        } catch (expected: IllegalStateException) {
            // expected
        }

        assertTrue(db.teamDao().getTeams().isEmpty())
        assertTrue(db.tournamentDao().getTournaments().isEmpty())
    }
}
