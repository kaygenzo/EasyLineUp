/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository

class ObserveTournaments(private val dao: TournamentRepository) {
    fun execute(): LiveData<List<Tournament>> = dao.observeTournaments()
}
