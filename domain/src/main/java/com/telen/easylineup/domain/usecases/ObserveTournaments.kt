/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow

class ObserveTournaments(private val dao: TournamentRepository) {
    operator fun invoke(): Flow<List<Tournament>> = dao.observeTournaments().asFlow()
}
