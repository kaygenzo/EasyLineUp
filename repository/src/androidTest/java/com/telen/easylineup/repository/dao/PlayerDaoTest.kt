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
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.RoomTournament
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [PlayerDao]'s hand-written JOIN queries against a real in-memory Room database: the
 * roster-with-optional-positions query behind the lineup screen, and the two shirt number history
 * queries. Repository tests mock the DAO, so none of this SQL runs anywhere else in isolation.
 */
@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PlayerDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.playerDao()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private suspend fun insertTeam(name: String): Long =
        db.teamDao().insertTeam(RoomTeam(name = name))

    private suspend fun insertTournament(): Long =
        db.tournamentDao().insertTournament(RoomTournament(name = "Fall Classic"))

    private suspend fun insertPlayer(teamId: Long, name: String, shirtNumber: Int = 0): Long =
        dao.insertPlayer(RoomPlayer(teamId = teamId, name = name, shirtNumber = shirtNumber))

    private suspend fun insertLineup(teamId: Long, tournamentId: Long, name: String): Long =
        db.lineupDao().insertLineup(
            RoomLineup(name = name, teamId = teamId, tournamentId = tournamentId)
        )

    private suspend fun assignPosition(playerId: Long, lineupId: Long, position: Int = 1) {
        db.playerFieldPositionsDao().insertPlayerFieldPositions(
            listOf(
                RoomPlayerFieldPosition(
                    playerId = playerId,
                    lineupId = lineupId,
                    position = position
                )
            )
        )
    }

    private suspend fun overrideNumber(playerId: Long, lineupId: Long, number: Int) {
        db.playerNumberOverlaysDao().insertPlayerNumberOverlay(
            RoomPlayerNumberOverlay(playerId = playerId, lineupId = lineupId, number = number)
        )
    }

    @Test
    fun teamPlayersQueryReturnsAssignedAndUnassignedPlayersSortedById() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val lineupId = insertLineup(teamId, insertTournament(), "Game 1")
        val assignedId = insertPlayer(teamId, "Assigned")
        val benchId = insertPlayer(teamId, "Bench")
        assignPosition(assignedId, lineupId, position = 4)

        val rows = dao.getTeamPlayersAndMaybePositions(lineupId).first()

        assertEquals(listOf(assignedId, benchId), rows.map { it.playerId })
        assertEquals(4, rows.first().position)
        assertTrue(rows.first().fieldPositionId > 0)
        assertEquals(0L, rows.last().fieldPositionId)
    }

    @Test
    fun teamPlayersQueryExcludesPlayersFromAnotherTeam() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val otherTeamId = insertTeam("Lakeside Bears")
        val lineupId = insertLineup(teamId, insertTournament(), "Game 1")
        insertPlayer(teamId, "Mine")
        insertPlayer(otherTeamId, "Theirs")

        val rows = dao.getTeamPlayersAndMaybePositions(lineupId).first()

        assertEquals(listOf("Mine"), rows.map { it.playerName })
    }

    @Test
    fun teamPlayersQueryDoesNotLeakAPositionFromAnotherLineup() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament()
        val lineupId = insertLineup(teamId, tournamentId, "Game 1")
        val otherLineupId = insertLineup(teamId, tournamentId, "Game 2")
        val playerId = insertPlayer(teamId, "Player")
        assignPosition(playerId, otherLineupId)

        val row = dao.getTeamPlayersAndMaybePositions(lineupId).first().single()

        assertEquals(lineupId, row.lineupId)
        assertEquals(0L, row.fieldPositionId)
    }

    @Test
    fun teamPlayersQueryEmitsAgainWhenAPositionIsAssigned() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val lineupId = insertLineup(teamId, insertTournament(), "Game 1")
        val playerId = insertPlayer(teamId, "Player")
        val flow = dao.getTeamPlayersAndMaybePositions(lineupId)
        assertEquals(0L, flow.first().single().fieldPositionId)

        assignPosition(playerId, lineupId)

        assertTrue(flow.first().single().fieldPositionId > 0)
    }

    @Test
    fun numberHistoryFromPlayersListsEveryLineupOfTheWearer() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament()
        val firstLineupId = insertLineup(teamId, tournamentId, "Game 1")
        val secondLineupId = insertLineup(teamId, tournamentId, "Game 2")
        val playerId = insertPlayer(teamId, "Wearer", shirtNumber = 7)
        assignPosition(playerId, firstLineupId)
        assignPosition(playerId, secondLineupId)

        val rows = dao.getShirtNumberHistoryFromPlayers(teamId, 7)

        assertEquals(setOf("Game 1", "Game 2"), rows.map { it.lineupName }.toSet())
        assertEquals(setOf("Wearer"), rows.map { it.playerName }.toSet())
        assertEquals(setOf(7), rows.map { it.number }.toSet())
    }

    @Test
    fun numberHistoryFromPlayersIgnoresAWearerWhoNeverPlayed() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        insertPlayer(teamId, "Never Played", shirtNumber = 7)

        assertTrue(dao.getShirtNumberHistoryFromPlayers(teamId, 7).isEmpty())
    }

    @Test
    fun numberHistoryFromPlayersIgnoresOtherNumbersAndOtherTeams() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val otherTeamId = insertTeam("Lakeside Bears")
        val tournamentId = insertTournament()
        val lineupId = insertLineup(teamId, tournamentId, "Game 1")
        val otherLineupId = insertLineup(otherTeamId, tournamentId, "Game 2")
        assignPosition(insertPlayer(teamId, "Other Number", shirtNumber = 8), lineupId)
        assignPosition(insertPlayer(otherTeamId, "Other Team", shirtNumber = 7), otherLineupId)

        assertTrue(dao.getShirtNumberHistoryFromPlayers(teamId, 7).isEmpty())
    }

    @Test
    fun numberHistoryFromOverlaysReturnsTheOverriddenNumberAndItsLineup() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val lineupId = insertLineup(teamId, insertTournament(), "Game 1")
        val playerId = insertPlayer(teamId, "Wearer", shirtNumber = 7)
        overrideNumber(playerId, lineupId, number = 42)

        val row = dao.getShirtNumberHistoryFromOverlays(teamId, 42).single()

        assertEquals(42, row.number)
        assertEquals(playerId, row.playerId)
        assertEquals(lineupId, row.lineupId)
        assertEquals("Game 1", row.lineupName)
    }

    @Test
    fun numberHistoryFromOverlaysIgnoresThePlayersOwnShirtNumber() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val lineupId = insertLineup(teamId, insertTournament(), "Game 1")
        val playerId = insertPlayer(teamId, "Wearer", shirtNumber = 7)
        overrideNumber(playerId, lineupId, number = 42)

        assertTrue(dao.getShirtNumberHistoryFromOverlays(teamId, 7).isEmpty())
    }

    @Test
    fun numberHistoryFromOverlaysIgnoresOtherTeams() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val otherTeamId = insertTeam("Lakeside Bears")
        val otherLineupId = insertLineup(otherTeamId, insertTournament(), "Game 1")
        overrideNumber(insertPlayer(otherTeamId, "Other Team"), otherLineupId, number = 42)

        assertTrue(dao.getShirtNumberHistoryFromOverlays(teamId, 42).isEmpty())
    }
}
