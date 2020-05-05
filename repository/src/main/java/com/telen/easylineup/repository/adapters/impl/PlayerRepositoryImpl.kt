package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.repository.dao.PlayerDao
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toPlayer
import com.telen.easylineup.repository.model.toPlayerWithPosition
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber

internal class PlayerRepositoryImpl(private val playerDao: PlayerDao): PlayerRepository {

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
            it.toPlayer()
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

    override fun getTeamPlayersAndMaybePositions(lineupID: Long): LiveData<List<PlayerWithPosition>> {
        return Transformations.map(playerDao.getTeamPlayersAndMaybePositions(lineupID)) {
            it.map { it.toPlayerWithPosition() }
        }
    }
}