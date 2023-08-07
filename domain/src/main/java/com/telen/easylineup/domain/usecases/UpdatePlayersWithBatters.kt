package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Single

internal class UpdatePlayersWithBatters :
    UseCase<UpdatePlayersWithBatters.RequestValues, UpdatePlayersWithBatters.ResponseValue>() {

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
            .firstOrNull { it.playerID == batter.playerID }
            ?.let { it.order = batter.playerOrder }
    }

    data class RequestValues(
        val players: List<PlayerWithPosition>,
        val batters: List<BatterState>,
    ) : UseCase.RequestValues

    object ResponseValue : UseCase.ResponseValue
}