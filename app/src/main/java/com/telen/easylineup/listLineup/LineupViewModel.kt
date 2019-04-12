package com.telen.easylineup.listLineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.Tournament
import io.reactivex.Single

class LineupViewModel: ViewModel() {
    val lineups = App.database.lineupDao().getAllLineup()

    fun getPlayerFieldPositionFor(lineup: Lineup): LiveData<List<PlayerFieldPosition>> {
        return App.database.lineupDao().getAllPlayerFieldPositionsForLineup(lineup.id)
    }

    fun createNewLineup(lineup: Lineup): Single<Long> {
        return App.database.lineupDao().insertLineup(lineup)
    }

    fun getLastEditedLineup(): LiveData<Lineup> {
        return App.database.lineupDao().getLastLineup()
    }

    fun getLineupsForTournament(tournament: Tournament): LiveData<List<Lineup>> {
        return App.database.lineupDao().getLineupsForTournament(tournament.id)
    }
}