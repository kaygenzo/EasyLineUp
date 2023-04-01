package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Single

internal class UpdatePlayersWithLineupMode :
    UseCase<UpdatePlayersWithLineupMode.RequestValues,
            UpdatePlayersWithLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            val players = requestValues.players
            val lineup = requestValues.lineup
            when (lineup.mode) {
                MODE_ENABLED -> {
                    when (requestValues.teamType) {
                        TeamType.SOFTBALL.id -> { /* nothing to do */
                        }
                        TeamType.BASEBALL.id -> {
                            // find the pitcher if exists and set him at position 10 in lineup
                            players.firstOrNull { it.isPitcher() }?.let {
                                // here we use directly the standard strategy because we only have
                                // one strategy in baseball
                                val strategy = TeamStrategy.getStrategyById(lineup.strategy)
                                it.order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
                                it.flags = PlayerFieldPosition.FLAG_FLEX
                            }
                        }
                        else -> {
                            return@defer Single.error(IllegalArgumentException())
                        }
                    }
                }
                MODE_DISABLED -> {
                    players.filter { it.isDpDhOrFlex() }.forEach { it.reset() }
                }
            }
            Single.just(ResponseValue())
        }
    }

    class RequestValues(
        val players: List<PlayerWithPosition>,
        val lineup: Lineup,
        val teamType: Int
    ) : UseCase.RequestValues

    class ResponseValue : UseCase.ResponseValue
}