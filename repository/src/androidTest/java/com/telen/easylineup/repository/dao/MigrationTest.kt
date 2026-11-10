/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.repository.RepositoryModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
                    "(2, 'Baseball Lineup', 2, 1, 0, ${TeamStrategy.STANDARD.id}, 0, 0, " +
                    "1000, 1000)"
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

    /**
     * The single-step tests above each start from a schema with only the rows that step needs.
     * This one is what a real user on version 16 goes through: every table populated, all three
     * migrations in one upgrade, then the result read back through the real Room DAOs. It catches
     * what the others can't - a step breaking the next one, or rows lost along the way.
     */
    @Test
    fun migrate19To20_turnsLicenseNumbersIntoTextAndKeepsTheirLineups() {
        helper.createDatabase(TEST_DB_NAME, 19).apply {
            execSQL(
                "INSERT INTO teams (id, name, type, main, hash, createdAt) " +
                    "VALUES (1, 'T', 1, 1, 'h', 0)"
            )
            execSQL(
                "INSERT INTO players (id, teamID, name, shirtNumber, licenseNumber, positions, " +
                    "pitching, batting, sex, hash) VALUES " +
                    "(1, 1, 'Licensed', 7, 22841, 0, 0, 0, 0, 'p1'), " +
                    "(2, 1, 'Unlicensed', 8, 0, 0, 0, 0, 0, 'p2')"
            )
            execSQL(
                "INSERT INTO tournaments (id, name, createdAt, startTime, endTime, hash) " +
                    "VALUES (1, 'Cup', 1, 0, 0, 'th')"
            )
            execSQL(
                "INSERT INTO lineups (id, name, teamID, tournamentID, mode, strategy, " +
                    "extraHitters, eventTime, createdAt, editedAt, roaster, hash) VALUES " +
                    "(1, 'L', 1, 1, 0, 0, 0, 0, 1, 1, NULL, 'lh')"
            )
            // A player's field positions reference the players table: rebuilding that table
            // must not cascade-delete them.
            execSQL(
                "INSERT INTO playerFieldPosition (id, playerID, lineupID, position, x, y, " +
                    "`order`, flags, hash) VALUES (1, 1, 1, 3, 0.5, 0.5, 1, 0, 'fp')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            20,
            true,
            RepositoryModule.migration19To20()
        )

        db.query("SELECT id, licenseNumber FROM players ORDER BY id").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("22841", cursor.getString(1))
            assertTrue(cursor.moveToNext())
            assertEquals("", cursor.getString(1))
        }
        db.query("SELECT COUNT(*) FROM playerFieldPosition").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migrate16To20_keepsEveryRowAndStaysReadableThroughTheDaos() {
        helper.createDatabase(TEST_DB_NAME, 16).apply {
            insertVersion16Rows()
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            20,
            true,
            RepositoryModule.migration16To17(),
            RepositoryModule.migration17To18(),
            RepositoryModule.migration18To19(),
            RepositoryModule.migration19To20()
        ).close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_NAME).build()
        try {
            runBlocking {
                val team = database.teamDao().getTeamById(1L)
                assertEquals("B5 Team", team.name)
                assertEquals("team-hash", team.hash)
                assertEquals(0L, team.createdAt)

                val tournament = database.tournamentDao().getTournaments().single()
                assertEquals("Tournament", tournament.name)
                assertEquals(1_000L, tournament.createdAt)
                assertEquals(0L, tournament.startTime)
                assertEquals(0L, tournament.endTime)
                assertNull(tournament.address)

                val lineup = database.lineupDao().getLineupById(1L)
                assertEquals("B5 Lineup", lineup.name)
                assertEquals(TeamStrategy.B5_DEFAULT.id, lineup.strategy)
                assertEquals("roster", lineup.roster)

                val player = database.playerDao().getPlayerById(1L)
                assertEquals("Player", player.name)
                assertEquals(7, player.shirtNumber)
                assertEquals("1000", player.licenseNumber)

                val position = database.playerFieldPositionsDao()
                    .getAllPlayersWithPositionsForLineup(1L)
                    .single()
                assertEquals("Player", position.playerName)
                assertEquals(3, position.position)
                assertEquals(0.5f, position.x)

                val overlay = database.playerNumberOverlaysDao()
                    .getPlayerNumberOverlays(1L)
                    .single()
                assertEquals(42, overlay.number)

                assertEquals(2, database.tilesDao().getTiles().single().type)
            }
        } finally {
            database.close()
        }
    }

    private fun SupportSQLiteDatabase.insertVersion16Rows() {
        execSQL(
            "INSERT INTO teams (id, name, type, main, hash) " +
                "VALUES (1, 'B5 Team', ${TeamType.BASEBALL_5.id}, 1, 'team-hash')"
        )
        execSQL(
            "INSERT INTO tournaments (id, name, createdAt, hash) " +
                "VALUES (1, 'Tournament', 1000, 'tournament-hash')"
        )
        execSQL(
            "INSERT INTO players (id, teamID, name, shirtNumber, licenseNumber, positions, " +
                "pitching, batting, sex, hash) " +
                "VALUES (1, 1, 'Player', 7, 1000, 0, 0, 0, 0, 'player-hash')"
        )
        execSQL(
            "INSERT INTO lineups (id, name, teamID, tournamentID, mode, strategy, extraHitters, " +
                "eventTime, createdAt, editedAt, roaster, hash) VALUES " +
                "(1, 'B5 Lineup', 1, 1, 0, ${TeamStrategy.STANDARD.id}, 0, 0, 1000, 1000, " +
                "'roster', 'lineup-hash')"
        )
        execSQL(
            "INSERT INTO playerFieldPosition (id, playerID, lineupID, position, x, y, `order`, " +
                "flags, hash) VALUES (1, 1, 1, 3, 0.5, 0.5, 1, 0, 'position-hash')"
        )
        execSQL(
            "INSERT INTO playerNumberOverlay (id, lineupID, playerID, number, hash) " +
                "VALUES (1, 1, 1, 42, 'overlay-hash')"
        )
        execSQL("INSERT INTO tiles (id, position, type, enabled) VALUES (1, 0, 2, 1)")
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }
}
