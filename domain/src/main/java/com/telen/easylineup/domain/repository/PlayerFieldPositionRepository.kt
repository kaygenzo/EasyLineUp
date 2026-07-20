/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.PositionWithLineup

interface PlayerFieldPositionRepository {
    suspend fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>)
    suspend fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>)
    suspend fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<PlayerFieldPosition>):
    Int
    suspend fun deletePosition(position: PlayerFieldPosition)
    suspend fun deletePositions(position: List<PlayerFieldPosition>)
    suspend fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition)
    suspend fun insertPlayerFieldPosition(fieldPosition: PlayerFieldPosition): Long
    suspend fun getPlayerFieldPositionByHash(hash: String): PlayerFieldPosition
    suspend fun getPlayerFieldPositions(): List<PlayerFieldPosition>
    suspend fun getPlayerFieldPosition(positionId: Long): PlayerFieldPosition
    suspend fun getAllPlayerFieldPositionsForLineup(lineupId: Long): List<PlayerFieldPosition>
    suspend fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): List<PlayerWithPosition>
    suspend fun getPlayerPositionFor(lineupId: Long, playerId: Long): PlayerFieldPosition?
    suspend fun getAllPositionsForPlayer(playerId: Long): List<PositionWithLineup>
    suspend fun getMostUsedPlayers(teamId: Long): List<PlayerGamesCount>
}
