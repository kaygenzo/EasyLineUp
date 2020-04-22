package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class NeedAssignPitcherFirstException : Exception()

class GetDPAndFlexFromPlayersInField: UseCase<GetDPAndFlexFromPlayersInField.RequestValues, GetDPAndFlexFromPlayersInField.ResponseValue>() {

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
                    ResponseValue(dp, flex, dpLocked, flexLocked, requestValues.teamType)
                }
    }

    class ResponseValue(val dp: Player?, val flex: Player?, val dpLocked: Boolean, val flexLocked: Boolean, val teamType: Int): UseCase.ResponseValue
    class RequestValues(val playersInLineup: List<PlayerWithPosition>, val teamType: Int): UseCase.RequestValues
}