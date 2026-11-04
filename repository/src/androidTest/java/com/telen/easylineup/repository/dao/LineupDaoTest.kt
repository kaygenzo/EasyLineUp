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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun getAllTournamentsWithLineupsOnlyReturnsTheRequestedTeam() = runBlocking {
        val teamAId = insertTeam("Riverside Hawks")
        val teamBId = insertTeam("Lakeside Tigers")
        val tournamentAId = insertTournament("Fall Classic", createdAt = 1_000L)
        val tournamentBId = insertTournament("Spring Cup", createdAt = 2_000L)
        insertLineup("Hawks Opener", teamAId, tournamentAId)
        insertLineup("Tigers Opener", teamBId, tournamentBId)

        val rows = lineupDao.getAllTournamentsWithLineups(filter = "", teamId = teamAId)

        assertEquals(1, rows.size)
        assertEquals("Fall Classic", rows.single().tournamentName)
        assertEquals("Hawks Opener", rows.single().lineupName)
    }

    @Test
    fun getAllTournamentsWithLineupsOrdersByTournamentCreatedAtDescending() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val olderTournamentId = insertTournament("Early Spring", createdAt = 1_000L)
        val newerTournamentId = insertTournament("Late Fall", createdAt = 5_000L)
        insertLineup("Lineup A", teamId, olderTournamentId)
        insertLineup("Lineup B", teamId, newerTournamentId)

        val rows = lineupDao.getAllTournamentsWithLineups(filter = "", teamId = teamId)

        assertEquals(listOf("Late Fall", "Early Spring"), rows.map { it.tournamentName })
    }

    @Test
    fun getAllTournamentsWithLineupsFilterMatchesEitherTournamentOrLineupName() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic", createdAt = 1_000L)
        insertLineup("Championship Game", teamId, tournamentId)

        val byTournamentName = lineupDao.getAllTournamentsWithLineups("Fall", teamId)
        val byLineupName = lineupDao.getAllTournamentsWithLineups("Championship", teamId)
        val noMatch = lineupDao.getAllTournamentsWithLineups("Nonexistent", teamId)

        assertEquals(1, byTournamentName.size)
        assertEquals(1, byLineupName.size)
        assertTrue(noMatch.isEmpty())
    }

    @Test
    fun getAllTournamentsWithLineupsExcludesATournamentWithNoLineupYet() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        insertTournament("Empty Tournament", createdAt = 1_000L)

        // A tournament with zero lineups never matches `teamID = :teamId` (that column only
        // exists on the `lineups` side of the LEFT JOIN) - locking this in because it's easy to
        // assume the LEFT JOIN alone would be enough to surface it.
        val rows = lineupDao.getAllTournamentsWithLineups(filter = "", teamId = teamId)

        assertTrue(rows.isEmpty())
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
}
