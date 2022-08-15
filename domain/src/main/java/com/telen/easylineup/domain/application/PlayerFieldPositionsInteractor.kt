package com.telen.easylineup.domain.application

import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.Subject

interface PlayerFieldPositionsInteractor {
    /** @deprecated **/
    fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>): Completable
    fun savePlayerFieldPosition(
        player: Player,
        position: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupID: Long?,
        lineupMode: Int,
        strategy: TeamStrategy,
        batterSize: Int,
        extraBatterSize: Int
    ): Completable

    fun deletePlayerPosition(
        player: Player,
        position: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        extraHitterSize: Int
    ): Completable

    fun switchPlayersPosition(
        p1: FieldPosition,
        p2: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable

    fun observeErrors(): Subject<DomainErrors.PlayerFieldPositions>
}