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
import com.telen.easylineup.repository.model.RoomTile
import com.telen.easylineup.repository.model.RoomTournament
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The hand-written DAO queries without a join: filters and sort orders that a mocked DAO would
 * never exercise. Room-generated insert/update/delete are left out, they carry no logic of ours.
 */
@RunWith(AndroidJUnit4::class)
class SimpleDaoQueriesTest {
    private lateinit var db: AppDatabase
    private var teamId = 0L
    private var otherTeamId = 0L
    private var tournamentId = 0L
    private var lineupId = 0L
    private var otherLineupId = 0L
    private var playerId = 0L
    private var otherPlayerId = 0L

    @Before
    fun createDatabase() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        teamId = db.teamDao().insertTeam(RoomTeam(name = "Team"))
        otherTeamId = db.teamDao().insertTeam(RoomTeam(name = "Other Team"))
        tournamentId = db.tournamentDao().insertTournament(
            RoomTournament(name = "Tournament", createdAt = 1_000L)
        )
        lineupId = db.lineupDao().insertLineup(
            RoomLineup(name = "Lineup", teamId = teamId, tournamentId = tournamentId)
        )
        otherLineupId = db.lineupDao().insertLineup(
            RoomLineup(name = "Other Lineup", teamId = otherTeamId, tournamentId = tournamentId)
        )
        playerId = db.playerDao().insertPlayer(RoomPlayer(teamId = teamId, name = "Player"))
        otherPlayerId = db.playerDao().insertPlayer(
            RoomPlayer(teamId = otherTeamId, name = "Other Player")
        )
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    @Test
    fun tilesComeBackSortedByPosition() = runBlocking {
        db.tilesDao().insertTiles(
            listOf(
                RoomTile(position = 2, type = 20),
                RoomTile(position = 0, type = 0),
                RoomTile(position = 1, type = 10)
            )
        )

        assertEquals(listOf(0, 10, 20), db.tilesDao().getTiles().map { it.type })
    }

    @Test
    fun tournamentsComeBackMostRecentFirst() = runBlocking {
        db.tournamentDao().insertTournament(RoomTournament(name = "Newer", createdAt = 2_000L))
        db.tournamentDao().insertTournament(RoomTournament(name = "Older", createdAt = 500L))

        assertEquals(
            listOf("Newer", "Tournament", "Older"),
            db.tournamentDao().getTournaments().map { it.name }
        )
        assertEquals(
            listOf("Newer", "Tournament", "Older"),
            db.tournamentDao().observeTournaments().first().map { it.name }
        )
    }

    @Test
    fun lineupsForTournamentAreLimitedToTheTeam() = runBlocking {
        val rows = db.lineupDao().getLineupsForTournament(tournamentId, teamId)

        assertEquals(listOf("Lineup"), rows.map { it.name })
    }

    @Test
    fun playersAreLimitedToTheTeam() = runBlocking {
        assertEquals(listOf(playerId), db.playerDao().getPlayersByTeamId(teamId).map { it.id })
        assertEquals(
            listOf(playerId),
            db.playerDao().observePlayersByTeamId(teamId).first().map { it.id }
        )
    }

    @Test
    fun observingAnUnknownRowEmitsAnEmptyList() = runBlocking {
        assertTrue(db.playerDao().observePlayerById(999L).first().isEmpty())
        assertTrue(db.lineupDao().observeLineupById(999L).first().isEmpty())
    }

    @Test
    fun playerPositionIsFoundForThatPlayerAndLineupOnly() = runBlocking {
        val dao = db.playerFieldPositionsDao()
        dao.insertPlayerFieldPositions(
            listOf(RoomPlayerFieldPosition(playerId = playerId, lineupId = lineupId, position = 4))
        )

        assertEquals(4, dao.getPlayerPositionFor(lineupId, playerId)?.position)
        assertNull(dao.getPlayerPositionFor(lineupId, otherPlayerId))
        assertNull(dao.getPlayerPositionFor(otherLineupId, playerId))
    }

    @Test
    fun positionsAreLimitedToTheLineup() = runBlocking {
        val dao = db.playerFieldPositionsDao()
        dao.insertPlayerFieldPositions(
            listOf(
                RoomPlayerFieldPosition(playerId = playerId, lineupId = lineupId),
                RoomPlayerFieldPosition(playerId = otherPlayerId, lineupId = otherLineupId)
            )
        )

        assertEquals(
            listOf(playerId),
            dao.getAllPlayerFieldPositionsForLineup(lineupId).map { it.playerId }
        )
    }

    @Test
    fun numberOverlaysAreLimitedToTheLineup() = runBlocking {
        val dao = db.playerNumberOverlaysDao()
        dao.insertPlayerNumberOverlays(
            listOf(
                RoomPlayerNumberOverlay(playerId = playerId, lineupId = lineupId, number = 1),
                RoomPlayerNumberOverlay(playerId = playerId, lineupId = otherLineupId, number = 2)
            )
        )

        assertEquals(listOf(1), dao.getPlayerNumberOverlays(lineupId).map { it.number })
        assertEquals(listOf(1), dao.observePlayerNumberOverlays(lineupId).first().map { it.number })
    }

    @Test
    fun numberOverlayIsFoundForThatPlayerAndLineupOnly() = runBlocking {
        val dao = db.playerNumberOverlaysDao()
        dao.insertPlayerNumberOverlay(
            RoomPlayerNumberOverlay(playerId = playerId, lineupId = lineupId, number = 7)
        )

        assertNotNull(dao.getShirtNumberOverlay(playerId, lineupId))
        assertNull(dao.getShirtNumberOverlay(otherPlayerId, lineupId))
        assertNull(dao.getShirtNumberOverlay(playerId, otherLineupId))
    }

    @Test
    fun updatesReportHowManyRowsChanged() = runBlocking {
        val lineup = db.lineupDao().getLineupById(lineupId)
        val unknown = RoomLineup(id = 999L, name = "Unknown", teamId = teamId)

        val count = db.lineupDao().updateLineupsWithRowCount(
            listOf(lineup.copy(name = "Renamed"), unknown)
        )

        assertEquals(1, count)
        assertEquals("Renamed", db.lineupDao().getLineupById(lineupId).name)
    }
}
