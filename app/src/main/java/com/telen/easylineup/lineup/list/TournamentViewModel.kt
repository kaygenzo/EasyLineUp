package com.telen.easylineup.lineup.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.DeleteTournament
import com.telen.easylineup.domain.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.GetTournaments
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single

class TournamentViewModel: ViewModel() {

    private val filterLiveData: MutableLiveData<String> = MutableLiveData()
    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val deleteTournamentUseCase = DeleteTournament(App.database.tournamentDao())
    private val getAllTournamentsWithLineups = GetAllTournamentsWithLineups(App.database.lineupDao())
    private val getTournaments = GetTournaments(App.database.tournamentDao())

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun registerFilterChanged() : LiveData<String> {
        return filterLiveData
    }

    fun getTournaments(): Single<List<Tournament>>{
        return UseCaseHandler.execute(getTournaments, GetTournaments.RequestValues()).map { it.tournaments }
    }

    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(getAllTournamentsWithLineups, GetAllTournamentsWithLineups.RequestValues(filter, it.team.id)) }
                .map { it.result }
    }

    fun deleteTournament(tournament: Tournament) : Completable {
        return UseCaseHandler.execute(deleteTournamentUseCase, DeleteTournament.RequestValues(tournament)).ignoreElement()
    }
}