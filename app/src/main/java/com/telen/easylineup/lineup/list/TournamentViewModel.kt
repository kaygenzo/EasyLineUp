package com.telen.easylineup.lineup.list

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Tournament
import io.reactivex.Single

class TournamentViewModel: ViewModel() {
    val tournaments = App.database.tournamentDao().getTournaments()

    fun insertTournamentIfNotExists(tournament: Tournament): Single<Long> {
        return if(tournament.id == 0L)
            App.database.tournamentDao().insertTournament(tournament)
        else
            Single.just(tournament.id)
    }
}