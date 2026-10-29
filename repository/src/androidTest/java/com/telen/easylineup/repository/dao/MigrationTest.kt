/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.repository.RepositoryModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the migrations for which a pre-migration schema is available (the schemas directory
 * only started being committed at version 16 - see repository/.gitignore). Every migration added
 * from now on should get a matching test here.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate16To17_addsTournamentDateAndAddressColumnsWithDefaults() {
        helper.createDatabase(TEST_DB_NAME, 16).apply {
            execSQL(
                "INSERT INTO tournaments (id, name, createdAt, hash) " +
                    "VALUES (1, 'Tournament', 1000, 'hash-1')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            17,
            true,
            RepositoryModule.migration16To17()
        )

        db.query("SELECT startTime, endTime, address FROM tournaments WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
            assertEquals(0L, cursor.getLong(1))
            assertTrue(cursor.isNull(2))
        }
    }

    @Test
    fun migrate17To18_addsTeamCreatedAtColumnWithDefault() {
        helper.createDatabase(TEST_DB_NAME, 17).apply {
            // type/main have no SQL-level DEFAULT in the exported v17 schema (ALTER TABLE ...
            // DEFAULT only takes effect on a real upgrade's live table, not on the schema
            // createDatabase() replays from the frozen 17.json) - must be supplied explicitly.
            execSQL(
                "INSERT INTO teams (id, name, type, main, hash) " +
                    "VALUES (1, 'Team', 0, 1, 'hash-1')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            18,
            true,
            RepositoryModule.migration17To18()
        )

        db.query("SELECT createdAt FROM teams WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
        }
    }

    @Test
    fun migrate18To19_fixesBaseball5LineupsStuckOnStandardStrategy() {
        helper.createDatabase(TEST_DB_NAME, 18).apply {
            execSQL(
                "INSERT INTO teams (id, name, type, main, createdAt) " +
                    "VALUES (1, 'B5 Team', ${TeamType.BASEBALL_5.id}, 1, 1000)"
            )
            execSQL(
                "INSERT INTO teams (id, name, type, main, createdAt) " +
                    "VALUES (2, 'Baseball Team', ${TeamType.BASEBALL.id}, 1, 1000)"
            )
            execSQL(
                "INSERT INTO tournaments (id, name, createdAt, startTime, endTime, hash) " +
                    "VALUES (1, 'Tournament', 1000, 0, 0, 'hash-1')"
            )
            execSQL(
                "INSERT INTO lineups (id, name, teamID, tournamentID, mode, strategy, " +
                    "extraHitters, eventTime, createdAt, editedAt) VALUES " +
                    "(1, 'B5 Lineup', 1, 1, 0, ${TeamStrategy.STANDARD.id}, 0, 0, 1000, 1000)"
            )
            execSQL(
                "INSERT INTO lineups (id, name, teamID, tournamentID, mode, strategy, " +
                    "extraHitters, eventTime, createdAt, editedAt) VALUES " +
                    "(2, 'Baseball Lineup', 2, 1, 0, ${TeamStrategy.STANDARD.id}, 0, 0, 1000, 1000)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            19,
            true,
            RepositoryModule.migration18To19()
        )

        db.query("SELECT strategy FROM lineups WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(TeamStrategy.B5_DEFAULT.id.toLong(), cursor.getLong(0))
        }
        db.query("SELECT strategy FROM lineups WHERE id = 2").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(TeamStrategy.STANDARD.id.toLong(), cursor.getLong(0))
        }
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }
}
