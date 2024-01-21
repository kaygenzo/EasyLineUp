/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import io.reactivex.rxjava3.core.Single

internal class GetDpAndFlexFromPlayersInField :
    UseCase<GetDpAndFlexFromPlayersInField.RequestValues,
GetDpAndFlexFromPlayersInField.ResponseValue>() {
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
                    TeamType.SOFTBALL.id -> players.firstOrNull { it.isFlex() }
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

    /**
     * @property configResult
     */
    class ResponseValue(val configResult: DpAndFlexConfiguration) : UseCase.ResponseValue
    /**
     * @property playersInLineup
     * @property teamType
     */
    class RequestValues(val playersInLineup: List<PlayerWithPosition>, val teamType: Int) :
        UseCase.RequestValues
}
