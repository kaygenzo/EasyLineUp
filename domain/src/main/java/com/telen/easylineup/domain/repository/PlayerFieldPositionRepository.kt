/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerGamesCount
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.PositionWithLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface PlayerFieldPositionRepository {
    fun insertPlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable
    fun updatePlayerFieldPositions(fieldPositions: List<PlayerFieldPosition>): Completable
    fun updatePlayerFieldPositionsWithRowCount(fieldPositions: List<PlayerFieldPosition>):
    Single<Int>
    fun deletePosition(position: PlayerFieldPosition): Completable
    fun deletePositions(position: List<PlayerFieldPosition>): Completable
    fun updatePlayerFieldPosition(fieldPosition: PlayerFieldPosition): Completable
    fun insertPlayerFieldPosition(fieldPosition: PlayerFieldPosition): Single<Long>
    fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>>
    fun getPlayerFieldPositionByHash(hash: String): Single<PlayerFieldPosition>
    fun getPlayerFieldPositions(): Single<List<PlayerFieldPosition>>
    fun getPlayerFieldPosition(positionId: Long): Single<PlayerFieldPosition>
    fun getAllPlayerFieldPositionsForLineup(lineupId: Long): Single<List<PlayerFieldPosition>>
    fun getAllPlayersWithPositionsForLineup(lineupId: Long): LiveData<List<PlayerWithPosition>>
    fun getAllPlayersWithPositionsForLineupRx(lineupId: Long): Single<List<PlayerWithPosition>>
    fun getPlayerPositionFor(lineupId: Long, playerId: Long): Maybe<PlayerFieldPosition>
    fun getAllPositionsForPlayer(playerId: Long): Single<List<PositionWithLineup>>
    fun getMostUsedPlayers(teamId: Long): Single<List<PlayerGamesCount>>
}
