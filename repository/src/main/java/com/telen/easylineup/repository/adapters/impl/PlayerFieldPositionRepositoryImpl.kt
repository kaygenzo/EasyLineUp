/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.PositionWithLineup
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.repository.dao.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toPlayerFieldPosition
import com.telen.easylineup.repository.model.toPlayerGamesCount
import com.telen.easylineup.repository.model.toPlayerWithPosition
import com.telen.easylineup.repository.model.toPositionWithLineup
import timber.log.Timber

internal class PlayerFieldPositionRepositoryImpl(private val playerFieldPositionsDao: PlayerFieldPositionsDao) :
    PlayerFieldPositionRepository {
    init {
        Timber.i("PlayerFieldPositionRepositoryImpl.init")
    }

    override suspend fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.insertPlayerFieldPositions(fieldPositions.map {
            RoomPlayerFieldPosition().init(
                it
            )
        })
    }

    override suspend fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.updatePlayerFieldPositions(fieldPositions.map {
            RoomPlayerFieldPosition().init(
                it
            )
        })
    }

    override suspend fun updatePlayerFieldPositionsWithRowCount(
        fieldPositions: List<PlayerFieldPosition>
    ): Int {
        return playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(fieldPositions.map {
            RoomPlayerFieldPosition().init(
                it
            )
        })
    }

    override suspend fun deletePosition(position: PlayerFieldPosition) {
        playerFieldPositionsDao.deletePositionById(position.id)
    }

    override suspend fun deletePositions(position: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.deletePositions(position.map {
            RoomPlayerFieldPosition().init(
                it
            )
        })
    }

    override suspend fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition) {
        playerFieldPositionsDao.updatePlayerFieldPosition(
            RoomPlayerFieldPosition().init(
                fieldPosition
            )
        )
    }

    override suspend fun insertPlayerFieldPosition(fieldPosition: PlayerFieldPosition): Long {
        return playerFieldPositionsDao.insertPlayerFieldPosition(
            RoomPlayerFieldPosition().init(
                fieldPosition
            )
        )
    }

    override suspend fun getPlayerFieldPositionByHash(hash: String): PlayerFieldPosition {
        return playerFieldPositionsDao.getPlayerFieldPositionByHash(hash).toPlayerFieldPosition()
    }

    override suspend fun getPlayerFieldPositions(): List<PlayerFieldPosition> {
        return playerFieldPositionsDao.getPlayerFieldPositions()
            .map { it.toPlayerFieldPosition() }
    }

    override suspend fun getPlayerFieldPosition(positionId: Long): PlayerFieldPosition {
        return playerFieldPositionsDao.getPlayerFieldPosition(positionId).toPlayerFieldPosition()
    }

    override suspend fun getAllPlayerFieldPositionsForLineup(
        lineupId: Long
    ): List<PlayerFieldPosition> {
        return playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(lineupId)
            .map { it.toPlayerFieldPosition() }
    }

    override suspend fun getAllPlayersWithPositionsForLineupRx(
        lineupId: Long
    ): List<PlayerWithPosition> {
        return playerFieldPositionsDao.getAllPlayersWithPositionsForLineupRx(lineupId)
            .map { it.toPlayerWithPosition() }
    }

    override suspend fun getPlayerPositionFor(
        lineupId: Long,
        playerId: Long
    ): PlayerFieldPosition? {
        return playerFieldPositionsDao.getPlayerPositionFor(lineupId, playerId)
            ?.toPlayerFieldPosition()
    }

    override suspend fun getAllPositionsForPlayer(playerId: Long): List<PositionWithLineup> {
        return playerFieldPositionsDao.getAllPositionsForPlayer(playerId)
            .map { it.toPositionWithLineup() }
    }

    override suspend fun getMostUsedPlayers(teamId: Long): List<PlayerGamesCount> {
        return playerFieldPositionsDao.getMostUsedPlayers(teamId)
            .map { it.toPlayerGamesCount() }
    }
}
