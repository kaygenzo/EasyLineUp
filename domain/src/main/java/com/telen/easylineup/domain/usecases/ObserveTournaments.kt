/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Flowable

class ObserveTournaments(private val dao: TournamentRepository) {
    operator fun invoke(): Flowable<List<Tournament>> = dao.observeTournaments()
}
