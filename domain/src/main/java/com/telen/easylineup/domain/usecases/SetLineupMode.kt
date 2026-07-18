/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Single

class SetLineupMode(
    private val getTeam: GetTeam,
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode
) : UseCase<SetLineupMode.RequestValues, SetLineupMode.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            val lineup = requestValues.lineup
            lineup.mode = if (requestValues.isEnabled) MODE_ENABLED else MODE_DISABLED
            getTeam.executeUseCase(GetTeam.RequestValues())
                .map { it.team }
                .flatMap {
                    val update = UpdatePlayersWithLineupMode.RequestValues(
                        requestValues.players,
                        lineup,
                        it.type
                    )
                    updatePlayersWithLineupMode.executeUseCase(update)
                }
                .map { ResponseValue() }
        }
    }

    /**
     * @property isEnabled
     * @property lineup
     * @property players
     */
    class RequestValues(
        val isEnabled: Boolean,
        val lineup: Lineup,
        val players: List<PlayerWithPosition>
    ) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
