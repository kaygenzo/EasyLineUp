package com.telen.easylineup.domain.application

import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable

interface PlayerFieldPositionsInteractor {
    /** @deprecated **/
    fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>): Completable
    fun savePlayerFieldPosition(
        player: Player,
        position: FieldPosition,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable

    fun deletePlayerPosition(
        player: Player,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        extraHitterSize: Int
    ): Completable

    fun switchPlayersPosition(
        p1: FieldPosition,
        p2: FieldPosition,
        list: List<PlayerWithPosition>,
        lineup: Lineup
    ): Completable
}