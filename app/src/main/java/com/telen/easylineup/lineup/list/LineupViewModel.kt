package com.telen.easylineup.lineup.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.data.Tournament
import io.reactivex.Single
import timber.log.Timber

class LineupViewModel: ViewModel() {
    val lineups = App.database.lineupDao().getAllLineup()

    fun getPlayerFieldPositionFor(lineup: Lineup): LiveData<List<PlayerFieldPosition>> {
        return App.database.lineupDao().getAllPlayerFieldPositionsForLineup(lineup.id)
    }

    fun getPlayersWithPositionsFor(lineup: Lineup): LiveData<List<PlayerWithPosition>> {
        return App.database.lineupDao().getAllPlayersWithPositionsForLineup(lineup.id)
    }

    fun createNewLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return insertTournamentIfNotExists(tournament)
                .flatMap { tournamentID ->
                    Timber.d("successfully inserted tournament or already existing, id: $tournamentID")
                    tournament.id = tournamentID
                    App.database.teamDao().getTeamsList()
                }
                .map { it.first() }
                .flatMap {team ->
                    val newLineup = Lineup(name = lineupTitle, teamId = team.id, tournamentId = tournament.id)
                    App.database.lineupDao().insertLineup(newLineup)
                }
    }

    fun getLastEditedLineup(): LiveData<Lineup> {
        return App.database.lineupDao().getLastLineup()
    }

    fun getLineupsForTournament(tournament: Tournament): LiveData<List<Lineup>> {
        return App.database.lineupDao().getLineupsForTournament(tournament.id)
    }

    fun getLineupByID(lineupID: Long): LiveData<Lineup> {
        return App.database.lineupDao().getLineupById(lineupID)
    }

    private fun insertTournamentIfNotExists(tournament: Tournament): Single<Long> {
        return if(tournament.id == 0L)
            App.database.tournamentDao().insertTournament(tournament)
        else
            Single.just(tournament.id)
    }
}