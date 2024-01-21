/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class PlayerRepositoryImpl(
    private val playerDao: PlayerDao,
    private val numberOverlayDao: PlayerNumberOverlayDao
) : PlayerRepository {
    init {
        Timber.i("PlayerRepositoryImpl.init")
    }

    override fun insertPlayer(player: Player): Single<Long> {
        return playerDao.insertPlayer(RoomPlayer().init(player))
    }

    override fun insertPlayers(players: List<Player>): Completable {
        return playerDao.insertPlayers(players.map { RoomPlayer().init(it) })
    }

    override fun deletePlayer(player: Player): Completable {
        return playerDao.deletePlayer(RoomPlayer().init(player))
    }

    override fun updatePlayer(player: Player): Completable {
        return playerDao.updatePlayer(RoomPlayer().init(player))
    }

    override fun updatePlayersWithRowCount(players: List<Player>): Single<Int> {
        return playerDao.updatePlayersWithRowCount(players.map { RoomPlayer().init(it) })
    }

    override fun getPlayerByHash(hash: String): Single<Player> {
        return playerDao.getPlayerByHash(hash).map { it.toPlayer() }
    }

    override fun getPlayerById(playerId: Long): LiveData<Player> {
        return playerDao.getPlayerById(playerId).map {
            // sometime the refresh it too quick and when the player is deleted, the player is null
            it?.toPlayer() ?: Player(teamId = 0, name = "", shirtNumber = 0, licenseNumber = 0)
        }
    }

    override fun getPlayerByIdAsSingle(playerId: Long): Single<Player> {
        return playerDao.getPlayerByIdAsSingle(playerId).map { it.toPlayer() }
    }

    override fun getPlayersByTeamId(teamId: Long): Single<List<Player>> {
        return playerDao.getPlayersByTeamId(teamId).map { it.map { it.toPlayer() } }
    }

    override fun getPlayers(): Single<List<Player>> {
        return playerDao.getPlayers().map { it.map { it.toPlayer() } }
    }

    override fun observePlayers(teamId: Long): LiveData<List<Player>> {
        return playerDao.getPlayersAsLiveData(teamId).map {
            it.map { it.toPlayer() }
        }
    }

    override fun getTeamPlayersAndMaybePositions(lineupId: Long):
    LiveData<List<PlayerWithPosition>> {
        return playerDao.getTeamPlayersAndMaybePositions(lineupId).map {
            it.map { it.toPlayerWithPosition() }
        }
    }

    override fun getShirtNumberFromPlayers(
        teamId: Long,
        number: Int
    ): Single<List<ShirtNumberEntry>> {
        return playerDao.getShirtNumberHistoryFromPlayers(teamId, number)
            .map { it.map { it.toShirtNumberEntry() } }
    }

    override fun getShirtNumberFromNumberOverlays(
        teamId: Long,
        number: Int
    ): Single<List<ShirtNumberEntry>> {
        return playerDao.getShirtNumberHistoryFromOverlays(teamId, number)
            .map { it.map { it.toShirtNumberEntry() } }
    }

    override fun getShirtNumberOverlay(
        playerId: Long,
        lineupId: Long
    ): Single<PlayerNumberOverlay> {
        return numberOverlayDao.getShirtNumberOverlay(playerId, lineupId)
            .map { it.toPlayerNumberOverlay() }
    }

    override fun observePlayersNumberOverlay(lineupId: Long): LiveData<List<PlayerNumberOverlay>> {
        return numberOverlayDao.observePlayerNumberOverlays(lineupId).map {
            it.map { it.toPlayerNumberOverlay() }
        }
    }

    override fun getPlayersNumberOverlay(lineupId: Long): Single<List<PlayerNumberOverlay>> {
        return numberOverlayDao.getPlayerNumberOverlays(lineupId)
            .map { it.map { it.toPlayerNumberOverlay() } }
    }

    override fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.deletePlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.updatePlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable {
        return numberOverlayDao.updatePlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay))
    }

    override fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.insertPlayerNumberOverlays(overlays.map {
            RoomPlayerNumberOverlay().init(
                it
            )
        })
    }

    override fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable {
        return numberOverlayDao.insertPlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay))
            .ignoreElement()
    }

    override fun getPlayerNumberOverlayByHash(hash: String): Single<PlayerNumberOverlay> {
        return numberOverlayDao.getPlayerNumberOverlayByHash(hash)
            .map { it.toPlayerNumberOverlay() }
    }
}
