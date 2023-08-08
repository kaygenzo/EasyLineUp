package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.PositionWithLineup
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.repository.dao.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class PlayerFieldPositionRepositoryImpl(private val playerFieldPositionsDao: PlayerFieldPositionsDao): PlayerFieldPositionRepository {

    init {
        Timber.i("PlayerFieldPositionRepositoryImpl.init")
    }

    override fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable {
        return playerFieldPositionsDao.insertPlayerFieldPositions(fieldPositions.map { RoomPlayerFieldPosition().init(it) })
    }

    override fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable {
        return playerFieldPositionsDao.updatePlayerFieldPositions(fieldPositions.map { RoomPlayerFieldPosition().init(it) })
    }

    override fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<PlayerFieldPosition>): Single<Int> {
        return playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(fieldPositions.map { RoomPlayerFieldPosition().init(it) })
    }

    override fun deletePosition(position: PlayerFieldPosition): Completable {
        return playerFieldPositionsDao.deletePosition(RoomPlayerFieldPosition().init(position))
    }

    override fun deletePositions(position: List<PlayerFieldPosition>): Completable {
        return playerFieldPositionsDao.deletePositions(position.map { RoomPlayerFieldPosition().init(it) })
    }

    override fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition): Completable {
        return playerFieldPositionsDao.updatePlayerFieldPosition(RoomPlayerFieldPosition().init(fieldPosition))
    }

    override fun insertPlayerFieldPosition(fieldPosition: PlayerFieldPosition): Single<Long> {
        return playerFieldPositionsDao.insertPlayerFieldPosition(RoomPlayerFieldPosition().init(fieldPosition))
    }

    override fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>> {
        return playerFieldPositionsDao.getAllPlayerFieldPositions().map {
            it.map { it.toPlayerFieldPosition() }
        }
    }

    override fun getPlayerFieldPositionByHash(hash: String): Single<PlayerFieldPosition> {
        return playerFieldPositionsDao.getPlayerFieldPositionByHash(hash).map { it.toPlayerFieldPosition() }
    }

    override fun getPlayerFieldPositions(): Single<List<PlayerFieldPosition>> {
        return playerFieldPositionsDao.getPlayerFieldPositions().map {  it.map { it.toPlayerFieldPosition() } }
    }

    override fun getPlayerFieldPosition(positionID: Long): Single<PlayerFieldPosition> {
        return playerFieldPositionsDao.getPlayerFieldPosition(positionID).map { it.toPlayerFieldPosition() }
    }

    override fun getAllPlayerFieldPositionsForLineup(lineupId: Long): Single<List<PlayerFieldPosition>> {
        return playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(lineupId).map {  it.map { it.toPlayerFieldPosition() } }
    }

    override fun getAllPlayersWithPositionsForLineup(lineupId: Long): LiveData<List<PlayerWithPosition>> {
        return playerFieldPositionsDao.getAllPlayersWithPositionsForLineup(lineupId).map {
            it.map { it.toPlayerWithPosition() }
        }
    }

    override fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): Single<List<PlayerWithPosition>> {
        return playerFieldPositionsDao.getAllPlayersWithPositionsForLineupRx(lineupId).map { it.map { it.toPlayerWithPosition() } }
    }

    override fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<PlayerFieldPosition> {
        return playerFieldPositionsDao.getPlayerPositionFor(lineupID, playerID).map { it.toPlayerFieldPosition() }
    }

    override fun getAllPositionsForPlayer(playerID: Long): Single<List<PositionWithLineup>> {
        return playerFieldPositionsDao.getAllPositionsForPlayer(playerID).map { it.map { it.toPositionWithLineup() } }
    }

    override fun getMostUsedPlayers(teamID: Long): Single<List<PlayerGamesCount>> {
        return playerFieldPositionsDao.getMostUsedPlayers(teamID).map { it.map { it.toPlayerGamesCount() } }
    }
}