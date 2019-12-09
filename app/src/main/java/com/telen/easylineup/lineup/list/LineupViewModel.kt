package com.telen.easylineup.lineup.list

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LineupViewModel: ViewModel() {
    val lineups = App.database.lineupDao().getAllLineup()

    private val getTeamUseCase = GetTeam(App.database.teamDao())

    fun createNewLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return insertTournamentIfNotExists(tournament)
                .flatMap { tournamentID ->
                    Timber.d("successfully inserted tournament or already existing, id: $tournamentID")
                    tournament.id = tournamentID
                    UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues(), observeOn = Schedulers.io())
                }
                .map { it.team }
                .flatMap {team ->
                    val newLineup = Lineup(name = lineupTitle, teamId = team.id, tournamentId = tournament.id)
                    App.database.lineupDao().insertLineup(newLineup)
                }
    }

    private fun insertTournamentIfNotExists(tournament: Tournament): Single<Long> {
        return if(tournament.id == 0L)
            App.database.tournamentDao().insertTournament(tournament)
        else
            App.database.tournamentDao().updateTournament(tournament).andThen(Single.just(tournament.id))
    }
}