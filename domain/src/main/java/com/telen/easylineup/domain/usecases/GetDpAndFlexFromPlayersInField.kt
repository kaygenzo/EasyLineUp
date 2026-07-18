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

class GetDpAndFlexFromPlayersInField(private val getTeam: GetTeam) :
    UseCase<GetDpAndFlexFromPlayersInField.RequestValues,
GetDpAndFlexFromPlayersInField.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getTeam.executeUseCase(GetTeam.RequestValues())
            .map { it.team.type }
            .flatMap { teamType ->
                Single.just(requestValues.playersInLineup)
                    .map { list ->
                        list.filter {
                            it.isAssigned() && !it.isSubstitute()
                        }
                    }
                    .map { players ->
                        val dpLocked = false
                        var flexLocked = false
                        val dp = players.firstOrNull { it.isDpDh() }

                        val flex = when (teamType) {
                            TeamType.SOFTBALL.id -> players.firstOrNull { it.isFlex() }
                            else -> {
                                flexLocked = true
                                players.firstOrNull { it.isPitcher() }
                            }
                        }
                        if (flex == null && teamType == TeamType.BASEBALL.id) {
                            throw NeedAssignPitcherFirstException()
                        }
                        ResponseValue(
                            DpAndFlexConfiguration(
                                dp,
                                flex,
                                dpLocked,
                                flexLocked,
                                teamType
                            )
                        )
                    }
            }
    }

    /**
     * @property configResult
     */
    class ResponseValue(val configResult: DpAndFlexConfiguration) : UseCase.ResponseValue
    /**
     * @property playersInLineup
     */
    class RequestValues(val playersInLineup: List<PlayerWithPosition>) :
        UseCase.RequestValues
}
