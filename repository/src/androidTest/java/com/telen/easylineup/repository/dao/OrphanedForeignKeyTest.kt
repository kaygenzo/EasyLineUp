/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Checks whether a `playerFieldPosition` row referencing a `playerID`/`lineupID` that does not
 * exist (a real scenario that could, in principle, arise from several years of migrations plus
 * player deletions) can even exist in this database, and if so, whether the INNER JOIN queries
 * that read it ([PlayerFieldPositionsDao.getMostUsedPlayers], etc.) would crash on it rather
 * than just filtering it out.
 *
 * Empirically: Room enforces FOREIGN KEY constraints by default - this in-memory database
 * refuses the orphaned insert outright with [SQLiteConstraintException], so the "does a stray
 * row crash a query" question never arises for data written through the DAOs. This test locks
 * that guarantee in: if a future change (a new Room version, a builder config change) silently
 * disabled foreign key enforcement, this test would start failing instead of the gap only
 * surfacing as a crash report from a corrupted installation.
 */
@RunWith(AndroidJUnit4::class)
class OrphanedForeignKeyTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    @Test
    fun insertingAPlayerFieldPositionForANonExistentPlayerAndLineupIsRejected() {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                db.playerFieldPositionsDao().insertPlayerFieldPositions(
                    listOf(
                        RoomPlayerFieldPosition(playerId = 999L, lineupId = 999L, position = 0)
                    )
                )
            }
        }
    }
}
