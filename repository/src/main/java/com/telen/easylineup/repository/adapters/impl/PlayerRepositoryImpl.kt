package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.dao.PlayerNumberOverlayDao
import com.telen.easylineup.repository.model.*
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toPlayer
import com.telen.easylineup.repository.model.toPlayerWithPosition
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class PlayerRepositoryImpl(private val playerDao: PlayerDao, private val numberOverlayDao: PlayerNumberOverlayDao): PlayerRepository {

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

    override fun getPlayerById(playerID: Long): LiveData<Player> {
        return Transformations.map(playerDao.getPlayerById(playerID)) {
            // sometime the refresh it too quick and when the player is deleted, the player is null
            it?.toPlayer() ?: Player(teamId = 0, name = "", shirtNumber = 0, licenseNumber = 0)
        }
    }

    override fun getPlayerByIdAsSingle(playerID: Long): Single<Player> {
        return playerDao.getPlayerByIdAsSingle(playerID).map { it.toPlayer() }
    }

    override fun getPlayers(teamID: Long): Single<List<Player>> {
        return playerDao.getPlayers(teamID).map { it.map { it.toPlayer() } }
    }

    override fun getPlayers(): Single<List<Player>> {
        return playerDao.getPlayers().map { it.map { it.toPlayer() } }
    }

    override fun observePlayers(teamID: Long): LiveData<List<Player>> {
        return Transformations.map(playerDao.getPlayersAsLiveData(teamID)) {
            it.map { it.toPlayer() }
        }
    }

    override fun getTeamPlayersAndMaybePositions(lineupID: Long): LiveData<List<PlayerWithPosition>> {
        return Transformations.map(playerDao.getTeamPlayersAndMaybePositions(lineupID)) {
            it.map { it.toPlayerWithPosition() }
        }
    }

    override fun getShirtNumberFromPlayers(teamID: Long, number: Int): Single<List<ShirtNumberEntry>> {
        return playerDao.getShirtNumberHistoryFromPlayers(teamID, number).map { it.map { it.toShirtNumberEntry() } }
    }

    override fun getShirtNumberFromNumberOverlays(teamID: Long, number: Int): Single<List<ShirtNumberEntry>> {
        return playerDao.getShirtNumberHistoryFromOverlays(teamID, number).map { it.map { it.toShirtNumberEntry() } }
    }

    override fun getShirtNumberOverlay(playerID: Long, lineupID: Long): Single<PlayerNumberOverlay> {
        return numberOverlayDao.getShirtNumberOverlay(playerID, lineupID).map { it.toPlayerNumberOverlay() }
    }

    override fun observePlayersNumberOverlay(lineupID: Long): LiveData<List<PlayerNumberOverlay>> {
        return Transformations.map(numberOverlayDao.observePlayerNumberOverlays(lineupID)) {
            it.map { it.toPlayerNumberOverlay() }
        }
    }

    override fun getPlayersNumberOverlay(lineupID: Long): Single<List<PlayerNumberOverlay>> {
        return numberOverlayDao.getPlayerNumberOverlays(lineupID).map { it.map { it.toPlayerNumberOverlay() } }
    }

    override fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.deletePlayerNumberOverlays(overlays.map { RoomPlayerNumberOverlay().init(it) })
    }

    override fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.updatePlayerNumberOverlays(overlays.map { RoomPlayerNumberOverlay().init(it) })
    }

    override fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable {
        return numberOverlayDao.updatePlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay))
    }

    override fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return numberOverlayDao.insertPlayerNumberOverlays(overlays.map { RoomPlayerNumberOverlay().init(it) })
    }

    override fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable {
        return numberOverlayDao.insertPlayerNumberOverlay(RoomPlayerNumberOverlay().init(overlay)).ignoreElement()
    }

    override fun getPlayerNumberOverlayByHash(hash: String): Single<PlayerNumberOverlay> {
        return numberOverlayDao.getPlayerNumberOverlayByHash(hash).map { it.toPlayerNumberOverlay() }
    }
}