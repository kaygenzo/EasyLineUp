/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Completable

interface PlayerFieldPositionsInteractor {
    /** @deprecated
     * @return **/
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
        position1: FieldPosition,
        position2: FieldPosition,
        list: List<PlayerWithPosition>,
        lineup: Lineup
    ): Completable
}
