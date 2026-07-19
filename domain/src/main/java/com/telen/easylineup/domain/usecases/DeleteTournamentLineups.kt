/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Completable

class DeleteTournamentLineups(
    private val lineupDao: LineupRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(tournament: Tournament): Completable {
        return getTeam()
            .flatMapCompletable { team ->
                lineupDao.getLineupsForTournamentRx(tournament.id, team.id)
                    .flatMapCompletable { lineupDao.deleteLineups(it) }
            }
            .subscribeOn(schedulersProvider.io())
    }
}
