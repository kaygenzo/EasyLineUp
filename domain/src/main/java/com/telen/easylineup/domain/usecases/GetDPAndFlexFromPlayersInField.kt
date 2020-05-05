package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import io.reactivex.Single

internal class GetDPAndFlexFromPlayersInField: UseCase<GetDPAndFlexFromPlayersInField.RequestValues, GetDPAndFlexFromPlayersInField.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.playersInLineup)
                .map { list ->
                    list.filter {
                        it.position >= FieldPosition.PITCHER.position && it.position <= FieldPosition.DP_DH.position
                    }
                }
                .map { players ->
                    val dpLocked = false
                    var flexLocked = false
                    val dp: Player? = players.filter { it.position == FieldPosition.DP_DH.position }
                            .map { it.toPlayer() }
                            .firstOrNull()

                    val flex = when(requestValues.teamType) {
                        TeamType.SOFTBALL.id -> {
                            players.filter { it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }
                                    .map { it.toPlayer() }
                                    .firstOrNull()
                        }
                        else -> {
                            flexLocked = true
                            players.filter { it.position == FieldPosition.PITCHER.position }
                                    .map { it.toPlayer() }
                                    .firstOrNull()
                        }
                    }
                    if(flex == null && requestValues.teamType == TeamType.BASEBALL.id) {
                        throw NeedAssignPitcherFirstException()
                    }
                    ResponseValue(DpAndFlexConfiguration(dp, flex, dpLocked, flexLocked, requestValues.teamType))
                }
    }

    class ResponseValue(val configResult: DpAndFlexConfiguration): UseCase.ResponseValue
    class RequestValues(val playersInLineup: List<PlayerWithPosition>, val teamType: Int): UseCase.RequestValues
}