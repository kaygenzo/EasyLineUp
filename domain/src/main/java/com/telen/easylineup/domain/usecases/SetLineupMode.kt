/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.rxjava3.core.Completable

class SetLineupMode(
    private val getTeam: GetTeam,
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(
        isEnabled: Boolean,
        lineup: Lineup,
        players: List<PlayerWithPosition>
    ): Completable {
        return Completable.defer {
            lineup.mode = if (isEnabled) MODE_ENABLED else MODE_DISABLED
            getTeam()
                .flatMapCompletable { team ->
                    updatePlayersWithLineupMode(players, lineup, team.type)
                }
        }.subscribeOn(schedulersProvider.io())
    }
}
