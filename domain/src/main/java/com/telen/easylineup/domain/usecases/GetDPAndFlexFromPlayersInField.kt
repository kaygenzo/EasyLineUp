package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import io.reactivex.rxjava3.core.Single

internal class GetDPAndFlexFromPlayersInField :
    UseCase<GetDPAndFlexFromPlayersInField.RequestValues,
            GetDPAndFlexFromPlayersInField.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.playersInLineup)
            .map { list ->
                list.filter {
                    it.isAssigned() && !it.isSubstitute()
                }
            }
            .map { players ->
                val dpLocked = false
                var flexLocked = false
                val dp = players.firstOrNull { it.isDpDh() }

                val flex = when (requestValues.teamType) {
                    TeamType.SOFTBALL.id -> {
                        players.firstOrNull { it.isFlex() }
                    }
                    else -> {
                        flexLocked = true
                        players.firstOrNull { it.isPitcher() }
                    }
                }
                if (flex == null && requestValues.teamType == TeamType.BASEBALL.id) {
                    throw NeedAssignPitcherFirstException()
                }
                ResponseValue(
                    DpAndFlexConfiguration(
                        dp,
                        flex,
                        dpLocked,
                        flexLocked,
                        requestValues.teamType
                    )
                )
            }
    }

    class ResponseValue(val configResult: DpAndFlexConfiguration) : UseCase.ResponseValue
    class RequestValues(val playersInLineup: List<PlayerWithPosition>, val teamType: Int) :
        UseCase.RequestValues
}