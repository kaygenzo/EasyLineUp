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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [PlayerFieldPositionsDao.getMostUsedPlayers] against a real in-memory Room database
 * instead of a mocked DAO. This exact query's tie-break behavior (`GROUP BY playerID ORDER BY 2
 * DESC`, no secondary sort key) already produced one false positive during the Compose
 * instrumented-test rewrite - a shirt-number search test had to switch shirt numbers because two
 * players tied on games-played and the query's own tie order collided with which one the UI
 * happened to pick. Locking that real, currently-undocumented behavior here so a query rewrite
 * that changes it is a deliberate decision, not a silent regression discovered the same way
 * again.
 */
@RunWith(AndroidJUnit4::class)
class PlayerFieldPositionsDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PlayerFieldPositionsDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.playerFieldPositionsDao()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private suspend fun insertTeam(name: String): Long =
        db.teamDao().insertTeam(RoomTeam(name = name))

    private suspend fun insertTournament(name: String): Long =
        db.tournamentDao().insertTournament(RoomTournament(name = name))

    private suspend fun insertPlayer(teamId: Long, name: String): Long =
        db.playerDao().insertPlayer(RoomPlayer(teamId = teamId, name = name))

    private suspend fun insertLineup(teamId: Long, tournamentId: Long, name: String): Long =
        db.lineupDao().insertLineup(
            RoomLineup(name = name, teamId = teamId, tournamentId = tournamentId)
        )

    /** One game played = one [RoomPlayerFieldPosition] row in its own lineup. */
    private suspend fun playGames(playerId: Long, teamId: Long, tournamentId: Long, count: Int) {
        repeat(count) { index ->
            val lineupId = insertLineup(teamId, tournamentId, "Game ${playerId}_$index")
            dao.insertPlayerFieldPositions(
                listOf(
                    RoomPlayerFieldPosition(playerId = playerId, lineupId = lineupId, position = 0)
                )
            )
        }
    }

    @Test
    fun ordersPlayersByGamesPlayedDescending() = runBlocking {
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic")
        val mostPlayedId = insertPlayer(teamId, "Starter 1")
        val leastPlayedId = insertPlayer(teamId, "Starter 2")
        playGames(mostPlayedId, teamId, tournamentId, count = 3)
        playGames(leastPlayedId, teamId, tournamentId, count = 1)

        val result = dao.getMostUsedPlayers(teamId)

        assertEquals(listOf(mostPlayedId, leastPlayedId), result.map { it.playerId })
        assertEquals(listOf(3, 1), result.map { it.size })
    }

    @Test
    fun excludesPlayersFromAnotherTeam() = runBlocking {
        val teamAId = insertTeam("Riverside Hawks")
        val teamBId = insertTeam("Lakeside Tigers")
        val tournamentId = insertTournament("Fall Classic")
        val teamAPlayerId = insertPlayer(teamAId, "Hawks Starter")
        val teamBPlayerId = insertPlayer(teamBId, "Tigers Starter")
        playGames(teamAPlayerId, teamAId, tournamentId, count = 1)
        playGames(teamBPlayerId, teamBId, tournamentId, count = 5)

        val result = dao.getMostUsedPlayers(teamAId)

        assertEquals(listOf(teamAPlayerId), result.map { it.playerId })
    }

    @Test
    fun aTieOnGamesPlayedBreaksByInsertionOrder() = runBlocking {
        // Documents the query's actual, currently undocumented tie-break (no secondary ORDER BY
        // key beyond COUNT(*) DESC) rather than leaving it to be rediscovered as a bug report -
        // if a future query rewrite changes this order, this test should fail and force that to
        // be a deliberate choice.
        val teamId = insertTeam("Riverside Hawks")
        val tournamentId = insertTournament("Fall Classic")
        val firstInsertedId = insertPlayer(teamId, "Starter 1")
        val secondInsertedId = insertPlayer(teamId, "Starter 2")
        playGames(firstInsertedId, teamId, tournamentId, count = 2)
        playGames(secondInsertedId, teamId, tournamentId, count = 2)

        val result = dao.getMostUsedPlayers(teamId)

        assertEquals(listOf(firstInsertedId, secondInsertedId), result.map { it.playerId })
    }
}
