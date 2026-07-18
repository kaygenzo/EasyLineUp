/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Completable

class InsertTournaments(
    private val dao: TournamentRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(tournaments: List<Tournament>): Completable {
        return dao.insertTournaments(tournaments).subscribeOn(schedulersProvider.io())
    }
}
