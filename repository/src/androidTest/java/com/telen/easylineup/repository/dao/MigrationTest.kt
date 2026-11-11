/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import android.database.sqlite.SQLiteConstraintException
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
    fun migrate20To21_indexesEveryForeignKeyColumn() {
        helper.createDatabase(TEST_DB_NAME, 20).close()

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            21,
            true,
            RepositoryModule.migration20To21()
        )

        // SQLite must now pick the new index for each foreign key lookup, not scan the table.
        listOf(
            "SELECT * FROM playerFieldPosition WHERE lineupID = 1" to
                "index_playerFieldPosition_lineupID",
            "SELECT * FROM players WHERE teamID = 1" to "index_players_teamID",
            "SELECT * FROM lineups WHERE teamID = 1" to "index_lineups_teamID",
            "SELECT * FROM lineups WHERE tournamentID = 1" to "index_lineups_tournamentID",
            "SELECT * FROM playerNumberOverlay WHERE lineupID = 1" to
                "index_playerNumberOverlay_lineupID",
            "SELECT * FROM playerNumberOverlay WHERE playerID = 1" to
                "index_playerNumberOverlay_playerID"
        ).forEach { (query, index) ->
            db.query("EXPLAIN QUERY PLAN $query").use { cursor ->
                assertTrue(cursor.moveToFirst())
                val plan = cursor.getString(cursor.getColumnIndexOrThrow("detail"))
                assertTrue("$query used: $plan", plan.contains(index))
            }
        }
    }

    @Test
    fun migrate21To22_mergesTournamentsSharingANameAndMakesNamesUnique() {
        helper.createDatabase(TEST_DB_NAME, 21).apply {
            execSQL("INSERT INTO teams (id, name, type, main, createdAt) VALUES (1, 'T', 0, 1, 0)")
            listOf(1 to "Cup", 2 to "Cup", 3 to "Open").forEach { (id, name) ->
                execSQL(
                    "INSERT INTO tournaments (id, name, createdAt, startTime, endTime) " +
                        "VALUES ($id, '$name', 0, 0, 0)"
                )
            }
            listOf(1 to 1, 2 to 2, 3 to 3).forEach { (id, tournamentId) ->
                execSQL(
                    "INSERT INTO lineups (id, name, teamID, tournamentID, mode, strategy, " +
                        "extraHitters, eventTime, createdAt, editedAt) " +
                        "VALUES ($id, 'L$id', 1, $tournamentId, 0, 0, 0, 0, 0, 0)"
                )
            }
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            22,
            true,
            RepositoryModule.migration21To22()
        )

        db.query("SELECT id FROM tournaments ORDER BY id").use { cursor ->
            val ids = generateSequence { if (cursor.moveToNext()) cursor.getLong(0) else null }
            assertEquals(listOf(1L, 3L), ids.toList())
        }
        db.query("SELECT tournamentID FROM lineups ORDER BY id").use { cursor ->
            val ids = generateSequence { if (cursor.moveToNext()) cursor.getLong(0) else null }
            // Both "Cup" lineups now sit in the kept "Cup"; "Open" is untouched.
            assertEquals(listOf(1L, 1L, 3L), ids.toList())
        }
        val duplicate = runCatching {
            db.execSQL(
                "INSERT INTO tournaments (name, createdAt, startTime, endTime) " +
                    "VALUES ('Cup', 0, 0, 0)"
            )
        }
        assertTrue(duplicate.exceptionOrNull() is SQLiteConstraintException)
    }

    @Test
    fun migrate22To23_givesEveryRowItsOwnHashAndMakesHashesUnique() {
        helper.createDatabase(TEST_DB_NAME, 22).apply {
            listOf(1 to "'same'", 2 to "'same'", 3 to "NULL", 4 to "''", 5 to "'own'")
                .forEach { (id, hash) ->
                    execSQL(
                        "INSERT INTO teams (id, name, type, main, createdAt, hash) " +
                            "VALUES ($id, 'T$id', 0, 0, 0, $hash)"
                    )
                }
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            23,
            true,
            RepositoryModule.migration22To23()
        )

        val hashes = db.query("SELECT id, hash FROM teams ORDER BY id").use { cursor ->
            generateSequence {
                if (cursor.moveToNext()) cursor.getLong(0) to cursor.getString(1) else null
            }.toMap()
        }
        // The oldest row keeps a shared hash, and a hash nobody else had is untouched.
        assertEquals("same", hashes[1L])
        assertEquals("own", hashes[5L])
        assertEquals(5, hashes.values.filterNot { it.isNullOrBlank() }.toSet().size)

        db.query("EXPLAIN QUERY PLAN SELECT * FROM players WHERE hash = 'x'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            val plan = cursor.getString(cursor.getColumnIndexOrThrow("detail"))
            assertTrue(plan, plan.contains("index_players_hash"))
        }
        val duplicate = runCatching {
            db.execSQL(
                "INSERT INTO teams (name, type, main, createdAt, hash) " +
                    "VALUES ('T6', 0, 0, 0, 'own')"
            )
        }
        assertTrue(duplicate.exceptionOrNull() is SQLiteConstraintException)
    }

    @Test
    fun migrate16ToLatest_keepsEveryRowAndStaysReadableThroughTheDaos() {
        helper.createDatabase(TEST_DB_NAME, 16).apply {
            insertVersion16Rows()
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            23,
            true,
            RepositoryModule.migration16To17(),
            RepositoryModule.migration17To18(),
            RepositoryModule.migration18To19(),
            RepositoryModule.migration19To20(),
            RepositoryModule.migration20To21(),
            RepositoryModule.migration21To22(),
            RepositoryModule.migration22To23()
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
