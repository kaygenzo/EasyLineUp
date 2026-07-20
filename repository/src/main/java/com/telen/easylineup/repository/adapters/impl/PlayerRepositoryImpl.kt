/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.dao.PlayerNumberOverlayDao
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toPlayer
import com.telen.easylineup.repository.model.toPlayerNumberOverlay
import com.telen.easylineup.repository.model.toPlayerWithPosition
import com.telen.easylineup.repository.model.toShirtNumberEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class PlayerRepositoryImpl(
    private val playerDao: PlayerDao,
    private val numberOverlayDao: PlayerNumberOverlayDao
) : PlayerRepository {
    init {
        Timber.i("PlayerRepositoryImpl.init")
    }

    override suspend fun insertPlayer(player: Player): Long {
        return playerDao.insertPlayer(RoomPlayer().init(player))
    }

    override suspend fun insertPlayers(players: List<Player>) {
        playerDao.insertPlayers(players.map { RoomPlayer().init(it) })
    }

    override suspend fun deletePlayer(player: Player) {
        playerDao.deletePlayer(RoomPlayer().init(player))
    }

    override suspend fun updatePlayer(player: Player) {
        playerDao.updatePlayer(RoomPlayer().init(player))
    }

    override suspend fun updatePlayersWithRowCount(players: List<Player>): Int {
        return playerDao.updatePlayersWithRowCount(players.map { RoomPlayer().init(it) })
    }

    override suspend fun getPlayerByHash(hash: String): Player {
        return playerDao.getPlayerByHash(hash).toPlayer()
    }

    override fun getPlayerById(playerId: Long): Flow<Player> {
        return playerDao.getPlayerById(playerId).map {
            // sometime the refresh it too quick and when the player is deleted, the player is null
            it.firstOrNull()?.toPlayer() ?: Player(
                teamId = 0,
                name = "",
                shirtNumber = 0,
                licenseNumber = 0
            )
        }
    }

    override suspend fun getPlayerByIdAsSingle(playerId: Long): Player {
        return playerDao.getPlayerByIdAsSingle(playerId).toPlayer()
    }

    override suspend fun getPlayersByTeamId(teamId: Long): List<Player> {
        return playerDao.getPlayersByTeamId(teamId).map { it.toPlayer() }
    }

    override suspend fun getPlayers(): List<Player> {
        return playerDao.getPlayers().map { it.toPlayer() }
    }

    override fun observePlayers(teamId: Long): Flow<List<Player>> {
        return playerDao.getPlayersAsFlowable(teamId).map { list ->
            list.map { it.toPlayer() }
        }
    }

    override fun getTeamPlayersAndMaybePositions(lineupId: Long): Flow<List<PlayerWithPosition>> {
        return playerDao.getTeamPlayersAndMaybePositions(lineupId).map { list ->
            list.map { it.toPlayerWithPosition() }
        }
    }

    override suspend fun getShirtNumberFromPlayers(
        teamId: Long,
        number: Int
    ): List<ShirtNumberEntry> {
        return playerDao.getShirtNumberHistoryFromPlayers(teamId, number)
            .map { it.toShirtNumberEntry() }
    }

    override suspend fun getShirtNumberFromNumberOverlays(
        teamId: Long,
        number: Int
    ): List<ShirtNumberEntry> {
        return playerDao.getShirtNumberHistoryFromOverlays(teamId, number)
            .map { it.toShirtNumberEntry() }
    }

    override suspend fun getShirtNumberOverlay(
        playerId: Long,
        lineupId: Long
    ): PlayerNumberOverlay {
        return numberOverlayDao.getShirtNumberOverlay(playerId, lineupId).toPlayerNumberOverlay()
    }

    override fun observePlayersNumberOverlay(lineupId: Long): Flow<List<PlayerNumberOverlay>> {
        return numberOverlayDao.observePlayerNumberOverlays(lineupId).map { list ->
            list.map { it.toPlayerNumberOverlay() }
        }
    }

    override suspend fun getPlayersNumberOverlay(lineupId: Long): List<PlayerNumberOverlay> {
        return numberOverlayDao.getPlayerNumberOverlays(lineupId)
            .map { it.toPlayerNumberOverlay() }
    }

    override suspend fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>) {
        numberOverlayDao.deletePlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override suspend fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>) {
        numberOverlayDao.updatePlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override suspend fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay) {
        numberOverlayDao.updatePlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay))
    }

    override suspend fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>) {
        numberOverlayDao.insertPlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override suspend fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay) {
        numberOverlayDao.insertPlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay))
    }

    override suspend fun getPlayerNumberOverlayByHash(hash: String): PlayerNumberOverlay {
        return numberOverlayDao.getPlayerNumberOverlayByHash(hash).toPlayerNumberOverlay()
    }
}
