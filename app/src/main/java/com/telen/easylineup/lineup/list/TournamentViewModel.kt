package com.telen.easylineup.lineup.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.DeleteTournament
import com.telen.easylineup.domain.GetAllTournamentsWithLineups
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.GetTournaments
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class TournamentViewModel: ViewModel(), KoinComponent {

    private val filterLiveData: MutableLiveData<String> = MutableLiveData()

    private val getTeamUseCase: GetTeam by inject()
    private val deleteTournamentUseCase: DeleteTournament by inject()
    private val getAllTournamentsWithLineups: GetAllTournamentsWithLineups by inject()
    private val getTournaments: GetTournaments by inject()

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