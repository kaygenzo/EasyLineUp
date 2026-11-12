/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.RoomTournament
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [LineupDao]'s two hand-written JOIN queries against a real in-memory Room database
 * instead of a mocked DAO - every existing repository test mocks the DAO itself, which proves
 * the repository impl calls the right method and maps its result, but never actually runs the
 * SQL. A JOIN/filter bug (wrong table prefix, wrong LEFT vs INNER join) would pass every one of
 * those tests and only surface here, against real rows across more than one team/tournament.
 */
@RunWith(AndroidJUnit4::class)
class LineupDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var lineupDao: LineupDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        lineupDao = db.lineupDao()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private suspend fun insertTeam(name: String): Long =
        db.teamDao().insertTeam(RoomTeam(name = name))

    private suspend fun insertTournament(name: String, createdAt: Long): Long =
        db.tournamentDao().insertTournament(RoomTournament(name = name, createdAt = createdAt))

    private suspend fun insertLineup(
        name: String,
        teamId: Long,
        tournamentId: Long
    ): Long = lineupDao.insertLineup(
        RoomLineup(name = name, teamId = teamId, tournamentId = tournamentId)
    )

    @Test
    fun observeAllTournamentsWithLineupsOnlyReturnsTheRequestedTeam() = runBlocking {
        val teamAId = insertTeam("Riverside Hawks")
        val teamBId = insertTeam("Lakeside Tigers")
        val tournamentAId = insertTournament("Fall Classic", createdAt = 1_000L)
        val tournamentBId = insertTournament("Spring Cup", createdAt = 2_000L)
        insertLineup("Hawks Opener", teamAId, tournamentAId)
        insertLineup("Tigers Opener", teamBId, tournamentBId)

        val rows = lineupDao.observeAllTournamentsWithLineups(filter = "", teamId = teamAId).first()

        assertEquals(1, rows.size)
        assertEquals("Fall Classic", rows.single().tournamentName)
        assertEquals("Hawks Opener", rows.single().lineupName)
    }

    @Test
    fun observeAllTournamentsWithLineupsOrdersByTournamentCreatedAtDescending() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val olderTournamentId = insertTournament("Early Spring", createdAt = 1_000L)
        val newerTournamentId = insertTournament("Late Fall", createdAt = 5_000L)
        insertLineup("Lineup A", teamId, olderTournamentId)
        insertLineup("Lineup B", teamId, newerTournamentId)

        val rows = lineupDao.observeAllTournamentsWithLineups(filter = "", teamId = teamId).first()

        assertEquals(listOf("Late Fall", "Early Spring"), rows.map { it.tournamentName })
    }

    @Test
    fun observeAllTournamentsWithLineupsFilterMatchesEitherTournamentOrLineupName() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        insertLineup("Championship Game", teamId, tournamentId)

        val byTournamentName = lineupDao.observeAllTournamentsWithLineups("Fall", teamId).first()
        val byLineupName =
            lineupDao.observeAllTournamentsWithLineups("Championship", teamId).first()
        val noMatch = lineupDao.observeAllTournamentsWithLineups("Nonexistent", teamId).first()

        assertEquals(1, byTournamentName.size)
        assertEquals(1, byLineupName.size)
        assertTrue(noMatch.isEmpty())
    }

    @Test
    fun observeAllTournamentsWithLineupsFilterTakesWildcardsLiterally() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        insertLineup("100% effort", teamId, insertTournament("Cup", createdAt = 1_000L))
        insertLineup("1000 runs", teamId, insertTournament("Open", createdAt = 2_000L))
        insertLineup("U_12 final", teamId, insertTournament("Youth", createdAt = 3_000L))
        insertLineup("U312 final", teamId, insertTournament("Masters", createdAt = 4_000L))

        val percent =
            lineupDao.observeAllTournamentsWithLineups("100%".escapeForLike(), teamId).first()
        val underscore =
            lineupDao.observeAllTournamentsWithLineups("U_1".escapeForLike(), teamId).first()

        assertEquals(listOf("100% effort"), percent.map { it.lineupName })
        assertEquals(listOf("U_12 final"), underscore.map { it.lineupName })
    }

    @Test
    fun observeAllTournamentsWithLineupsIncludesATournamentWithNoLineupYet() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Empty Tournament", createdAt = 1_000L)

        val rows = lineupDao.observeAllTournamentsWithLineups(filter = "", teamId = teamId).first()

        assertEquals(tournamentId, rows.single().tournamentId)
        assertEquals(0L, rows.single().lineupId)
    }

    @Test
    fun observeAllTournamentsWithLineupsFiltersAnEmptyTournamentByItsName() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        insertTournament("Empty Tournament", createdAt = 1_000L)

        val match = lineupDao.observeAllTournamentsWithLineups("Empty", teamId).first()
        val noMatch = lineupDao.observeAllTournamentsWithLineups("Fall", teamId).first()

        assertEquals(1, match.size)
        assertTrue(noMatch.isEmpty())
    }

    @Test
    fun observeAllTournamentsWithLineupsLeavesOutATournamentOnlyAnotherTeamPlaysIn() =
        runBlocking {
            val teamAId = insertTeam("Riverside Hawks")
            val teamBId = insertTeam("Lakeside Tigers")
            val sharedId = insertTournament("Fall Classic", createdAt = 1_000L)
            insertLineup("Hawks Opener", teamAId, sharedId)
            insertLineup("Tigers Opener", teamBId, sharedId)
            val tigersOnlyId = insertTournament("Spring Cup", createdAt = 2_000L)
            insertLineup("Tigers Final", teamBId, tigersOnlyId)

            val rows = lineupDao.observeAllTournamentsWithLineups("", teamAId).first()

            assertEquals(listOf("Hawks Opener"), rows.map { it.lineupName })
        }

    @Test
    fun hasLineupsForTournamentCountsEveryTeam() = runBlocking {
        val teamId = insertTeam("Lakeside Tigers")
        val playedId = insertTournament("Fall Classic", createdAt = 1_000L)
        insertLineup("Tigers Opener", teamId, playedId)
        val emptyId = insertTournament("Spring Cup", createdAt = 2_000L)

        assertTrue(lineupDao.hasLineupsForTournament(playedId))
        assertFalse(lineupDao.hasLineupsForTournament(emptyId))
    }

    @Test
    fun getAllPlayerPositionsForTournamentJoinsPlayerNamesForAssignedPositions() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        val lineupId = insertLineup("Opening Day", teamId, tournamentId)
        val playerId = db.playerDao().insertPlayer(RoomPlayer(teamId = teamId, name = "Jonas"))
        db.playerFieldPositionsDao().insertPlayerFieldPositions(
            listOf(RoomPlayerFieldPosition(playerId = playerId, lineupId = lineupId, position = 3))
        )

        val rows = lineupDao.getAllPlayerPositionsForTournament(tournamentId, teamId)

        assertEquals(1, rows.size)
        assertEquals("Jonas", rows.single().playerName)
        assertEquals(3, rows.single().position)
    }

    @Test
    fun getAllPlayerPositionsForTournamentKeepsALineupWithNoPositionsAssignedYet() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        insertLineup("Empty Lineup", teamId, tournamentId)

        // LEFT JOIN: a lineup with zero playerFieldPosition rows must still come back (as the
        // one row the stats screens need to know the lineup exists), with the joined columns
        // null rather than the whole lineup disappearing.
        val rows = lineupDao.getAllPlayerPositionsForTournament(tournamentId, teamId)

        assertEquals(1, rows.size)
        assertNull(rows.single().playerName)
        assertNull(rows.single().playerId)
        assertNull(rows.single().position)
    }

    @Test
    fun getAllPlayerPositionsForTournamentExcludesOtherTournaments() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentAId = insertTournament("Fall Classic", createdAt = 1_000L)
        val tournamentBId = insertTournament("Spring Cup", createdAt = 2_000L)
        insertLineup("Fall Lineup", teamId, tournamentAId)
        insertLineup("Spring Lineup", teamId, tournamentBId)

        val rows = lineupDao.getAllPlayerPositionsForTournament(tournamentAId, teamId)

        assertEquals(1, rows.size)
        assertEquals("Fall Lineup", rows.single().lineupName)
    }

    @Test
    fun getLastLineupReturnsTheMostRecentlyEditedLineupOfTheTeam() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        lineupDao.insertLineup(editedLineup("Older", teamId, tournamentId, editedAt = 1_000L))
        lineupDao.insertLineup(editedLineup("Latest", teamId, tournamentId, editedAt = 3_000L))
        lineupDao.insertLineup(editedLineup("Middle", teamId, tournamentId, editedAt = 2_000L))

        assertEquals("Latest", lineupDao.getLastLineup(teamId)?.name)
    }

    @Test
    fun getLastLineupIgnoresAMoreRecentLineupFromAnotherTeam() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val otherTeamId = insertTeam("Lakeside Bears")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        lineupDao.insertLineup(editedLineup("Mine", teamId, tournamentId, editedAt = 1_000L))
        lineupDao.insertLineup(editedLineup("Theirs", otherTeamId, tournamentId, editedAt = 2_000L))

        assertEquals("Mine", lineupDao.getLastLineup(teamId)?.name)
    }

    @Test
    fun getLastLineupReturnsNullForATeamWithoutLineups() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")

        assertNull(lineupDao.getLastLineup(teamId))
    }

    private fun editedLineup(
        name: String,
        teamId: Long,
        tournamentId: Long,
        editedAt: Long
    ) = RoomLineup(
        name = name,
        teamId = teamId,
        tournamentId = tournamentId,
        editedTimeInMillis = editedAt
    )
}
