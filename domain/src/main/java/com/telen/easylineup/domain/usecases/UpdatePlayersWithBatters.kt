/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Single

internal class UpdatePlayersWithBatters :
    UseCase<UpdatePlayersWithBatters.RequestValues, UpdatePlayersWithBatters.ResponseValue>() {
    object ResponseValue : UseCase.ResponseValue

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.fromCallable {
            with(requestValues) {
                batters.forEach { apply(players, it) }
            }
            ResponseValue
        }
    }

    private fun apply(players: List<PlayerWithPosition>, batter: BatterState) {
        players
            .firstOrNull { it.playerId == batter.playerId }
            ?.let { it.order = batter.playerOrder }
    }

    /**
     * @property players
     * @property batters
     */
    data class RequestValues(
        val players: List<PlayerWithPosition>,
        val batters: List<BatterState>,
    ) : UseCase.RequestValues
}
