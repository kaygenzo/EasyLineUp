/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Single

class GetTournaments(
    private val dao: TournamentRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(): Single<List<Tournament>> {
        return dao.getTournaments().subscribeOn(schedulersProvider.io())
    }
}
