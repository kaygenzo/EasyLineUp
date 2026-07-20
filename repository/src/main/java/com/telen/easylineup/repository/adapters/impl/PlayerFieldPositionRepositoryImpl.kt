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
import com.telen.easylineup.repository.model.toDomain
import com.telen.easylineup.repository.model.toRoom
import timber.log.Timber

internal class PlayerFieldPositionRepositoryImpl(private val playerFieldPositionsDao: PlayerFieldPositionsDao) :
    PlayerFieldPositionRepository {
    init {
        Timber.i("PlayerFieldPositionRepositoryImpl.init")
    }

    override suspend fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.insertPlayerFieldPositions(fieldPositions.map { it.toRoom() })
    }

    override suspend fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.updatePlayerFieldPositions(fieldPositions.map { it.toRoom() })
    }

    override suspend fun updatePlayerFieldPositionsWithRowCount(
        fieldPositions: List<PlayerFieldPosition>
    ): Int {
        return playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(
            fieldPositions = fieldPositions.map { it.toRoom() }
        )
    }

    override suspend fun deletePosition(position: PlayerFieldPosition) {
        playerFieldPositionsDao.deletePositionById(position.id)
    }

    override suspend fun deletePositions(position: List<PlayerFieldPosition>) {
        playerFieldPositionsDao.deletePositions(position.map { it.toRoom() })
    }

    override suspend fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition) {
        playerFieldPositionsDao.updatePlayerFieldPosition(fieldPosition.toRoom())
    }

    override suspend fun insertPlayerFieldPosition(fieldPosition: PlayerFieldPosition): Long {
        return playerFieldPositionsDao.insertPlayerFieldPosition(fieldPosition.toRoom())
    }

    override suspend fun getPlayerFieldPositionByHash(hash: String): PlayerFieldPosition {
        return playerFieldPositionsDao.getPlayerFieldPositionByHash(hash).toDomain()
    }

    override suspend fun getPlayerFieldPositions(): List<PlayerFieldPosition> {
        return playerFieldPositionsDao.getPlayerFieldPositions()
            .map { it.toDomain() }
    }

    override suspend fun getPlayerFieldPosition(positionId: Long): PlayerFieldPosition {
        return playerFieldPositionsDao.getPlayerFieldPosition(positionId).toDomain()
    }

    override suspend fun getAllPlayerFieldPositionsForLineup(
        lineupId: Long
    ): List<PlayerFieldPosition> {
        return playerFieldPositionsDao.getAllPlayerFieldPositionsForLineup(lineupId)
            .map { it.toDomain() }
    }

    override suspend fun getAllPlayersWithPositionsForLineupRx(
        lineupId: Long
    ): List<PlayerWithPosition> {
        return playerFieldPositionsDao.getAllPlayersWithPositionsForLineupRx(lineupId)
            .map { it.toDomain() }
    }

    override suspend fun getPlayerPositionFor(
        lineupId: Long,
        playerId: Long
    ): PlayerFieldPosition? {
        return playerFieldPositionsDao.getPlayerPositionFor(lineupId, playerId)
            ?.toDomain()
    }

    override suspend fun getAllPositionsForPlayer(playerId: Long): List<PositionWithLineup> {
        return playerFieldPositionsDao.getAllPositionsForPlayer(playerId)
            .map { it.toDomain() }
    }

    override suspend fun getMostUsedPlayers(teamId: Long): List<PlayerGamesCount> {
        return playerFieldPositionsDao.getMostUsedPlayers(teamId)
            .map { it.toDomain() }
    }
}
