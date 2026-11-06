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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Import decides between insert and update by looking each exported row up by its hash, so a
 * lookup that misses creates duplicates and one that matches the wrong row overwrites it. Every
 * entity gets a decoy row with a different hash to prove the lookup really filters.
 */
@RunWith(AndroidJUnit4::class)
class HashLookupDaoTest {
    private lateinit var db: AppDatabase
    private var teamId = 0L
    private var tournamentId = 0L
    private var playerId = 0L
    private var lineupId = 0L

    @Before
    fun createDatabase() = runBlocking<Unit> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        teamId = db.teamDao().insertTeam(RoomTeam(name = "Team", hash = HASH))
        db.teamDao().insertTeam(RoomTeam(name = "Decoy", hash = DECOY_HASH))
        tournamentId = db.tournamentDao().insertTournament(
            RoomTournament(name = "Tournament", hash = HASH)
        )
        db.tournamentDao().insertTournament(RoomTournament(name = "Decoy", hash = DECOY_HASH))
        playerId = db.playerDao().insertPlayer(
            RoomPlayer(teamId = teamId, name = "Player", hash = HASH)
        )
        db.playerDao().insertPlayer(RoomPlayer(teamId = teamId, name = "Decoy", hash = DECOY_HASH))
        lineupId = db.lineupDao().insertLineup(
            RoomLineup(name = "Lineup", teamId = teamId, tournamentId = tournamentId, hash = HASH)
        )
        db.lineupDao().insertLineup(
            RoomLineup(
                name = "Decoy",
                teamId = teamId,
                tournamentId = tournamentId,
                hash = DECOY_HASH
            )
        )
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    @Test
    fun teamIsFoundByHash() = runBlocking {
        assertEquals("Team", db.teamDao().getTeamByHash(HASH)?.name)
        assertNull(db.teamDao().getTeamByHash(UNKNOWN_HASH))
    }

    @Test
    fun tournamentIsFoundByHash() = runBlocking {
        assertEquals("Tournament", db.tournamentDao().getTournamentByHash(HASH)?.name)
        assertNull(db.tournamentDao().getTournamentByHash(UNKNOWN_HASH))
    }

    @Test
    fun playerIsFoundByHash() = runBlocking {
        assertEquals("Player", db.playerDao().getPlayerByHash(HASH)?.name)
        assertNull(db.playerDao().getPlayerByHash(UNKNOWN_HASH))
    }

    @Test
    fun lineupIsFoundByHash() = runBlocking {
        assertEquals("Lineup", db.lineupDao().getLineupByHash(HASH)?.name)
        assertNull(db.lineupDao().getLineupByHash(UNKNOWN_HASH))
    }

    @Test
    fun playerFieldPositionIsFoundByHash() = runBlocking {
        val dao = db.playerFieldPositionsDao()
        dao.insertPlayerFieldPositions(
            listOf(
                RoomPlayerFieldPosition(
                    playerId = playerId,
                    lineupId = lineupId,
                    position = 1,
                    hash = HASH
                ),
                RoomPlayerFieldPosition(
                    playerId = playerId,
                    lineupId = lineupId,
                    position = 2,
                    hash = DECOY_HASH
                )
            )
        )

        assertEquals(1, dao.getPlayerFieldPositionByHash(HASH)?.position)
        assertNull(dao.getPlayerFieldPositionByHash(UNKNOWN_HASH))
    }

    @Test
    fun playerNumberOverlayIsFoundByHash() = runBlocking {
        val dao = db.playerNumberOverlaysDao()
        dao.insertPlayerNumberOverlays(
            listOf(
                RoomPlayerNumberOverlay(
                    playerId = playerId,
                    lineupId = lineupId,
                    number = 1,
                    hash = HASH
                ),
                RoomPlayerNumberOverlay(
                    playerId = playerId,
                    lineupId = lineupId,
                    number = 2,
                    hash = DECOY_HASH
                )
            )
        )

        assertEquals(1, dao.getPlayerNumberOverlayByHash(HASH)?.number)
        assertNull(dao.getPlayerNumberOverlayByHash(UNKNOWN_HASH))
    }

    companion object {
        private const val HASH = "hash"
        private const val DECOY_HASH = "decoy-hash"
        private const val UNKNOWN_HASH = "unknown-hash"
    }
}
