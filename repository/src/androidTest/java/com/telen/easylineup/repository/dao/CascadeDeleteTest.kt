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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Locks in what each delete takes down with it, through the schema's real `ON DELETE CASCADE`
 * foreign keys. Domain use cases only delete the parent row and rely on this, so a dropped
 * cascade would leave orphans behind - or a new one would silently destroy a user's data.
 */
@RunWith(AndroidJUnit4::class)
class CascadeDeleteTest {
    private lateinit var db: AppDatabase
    private lateinit var team: RoomTeam
    private lateinit var tournament: RoomTournament
    private lateinit var player: RoomPlayer
    private lateinit var lineup: RoomLineup

    @Before
    fun createDatabaseWithOneOfEverything() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        team = RoomTeam(name = "Riverside Hawks")
            .let { it.copy(id = db.teamDao().insertTeam(it)) }
        tournament = RoomTournament(name = "Fall Classic")
            .let { it.copy(id = db.tournamentDao().insertTournament(it)) }
        player = RoomPlayer(teamId = team.id, name = "Player")
            .let { it.copy(id = db.playerDao().insertPlayer(it)) }
        lineup = RoomLineup(name = "Game 1", teamId = team.id, tournamentId = tournament.id)
            .let { it.copy(id = db.lineupDao().insertLineup(it)) }
        db.playerFieldPositionsDao().insertPlayerFieldPositions(
            listOf(RoomPlayerFieldPosition(playerId = player.id, lineupId = lineup.id))
        )
        db.playerNumberOverlaysDao().insertPlayerNumberOverlay(
            RoomPlayerNumberOverlay(playerId = player.id, lineupId = lineup.id, number = 42)
        )
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private suspend fun assertPositionsAndOverlaysAreGone() {
        assertTrue(db.playerFieldPositionsDao().getPlayerFieldPositions().isEmpty())
        assertTrue(db.playerNumberOverlaysDao().getPlayerNumberOverlays(lineup.id).isEmpty())
    }

    @Test
    fun deletingATeamDeletesItsPlayersAndLineupsButNotTheTournament() = runBlocking {
        db.teamDao().deleteTeam(team)

        assertTrue(db.playerDao().getPlayers().isEmpty())
        assertTrue(db.lineupDao().getLineups().isEmpty())
        assertPositionsAndOverlaysAreGone()
        assertEquals(1, db.tournamentDao().getTournaments().size)
    }

    @Test
    fun deletingATournamentDeletesItsLineupsButNotTheTeamOrItsPlayers() = runBlocking {
        db.tournamentDao().deleteTournament(tournament)

        assertTrue(db.lineupDao().getLineups().isEmpty())
        assertPositionsAndOverlaysAreGone()
        assertEquals(1, db.teamDao().getTeams().size)
        assertEquals(1, db.playerDao().getPlayers().size)
    }

    @Test
    fun deletingALineupDeletesItsPositionsAndOverlaysButNotThePlayer() = runBlocking {
        db.lineupDao().deleteLineup(lineup)

        assertPositionsAndOverlaysAreGone()
        assertEquals(1, db.playerDao().getPlayers().size)
        assertEquals(1, db.tournamentDao().getTournaments().size)
    }

    @Test
    fun deletingAPlayerDeletesItsPositionsAndOverlaysButNotTheLineup() = runBlocking {
        db.playerDao().deletePlayer(player)

        assertPositionsAndOverlaysAreGone()
        assertEquals(1, db.lineupDao().getLineups().size)
    }
}
