/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class GetDpAndFlexFromPlayersInField(
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(playersInLineup: List<PlayerWithPosition>): Result<DpAndFlexConfiguration> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                val teamType = getTeam().await().type
                val players = playersInLineup.filter {
                    it.isAssigned() && !it.isSubstitute()
                }

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
                DpAndFlexConfiguration(
                    dp,
                    flex,
                    dpLocked,
                    flexLocked,
                    teamType
                )
            }
        }
}
