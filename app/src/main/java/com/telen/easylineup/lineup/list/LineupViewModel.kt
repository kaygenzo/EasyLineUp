package com.telen.easylineup.lineup.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class LineupViewModel: ViewModel(), KoinComponent {

    private val filterLiveData: MutableLiveData<String> = MutableLiveData()
    private var chosenRoaster: GetRoaster.ResponseValue? = null

    private val createLineupUseCase: CreateLineup by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val deleteTournamentUseCase: DeleteTournament by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineups by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val getRoasterUseCase: GetRoaster by inject()

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun registerFilterChanged() : LiveData<String> {
        return filterLiveData
    }

    fun getTournaments(): Single<List<Tournament>>{
        return UseCaseHandler.execute(getTournamentsUseCase, GetTournaments.RequestValues()).map { it.tournaments }
    }

    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(getAllTournamentsWithLineupsUseCase, GetAllTournamentsWithLineups.RequestValues(filter, it.team.id)) }
                .map { it.result }
    }

    fun deleteTournament(tournament: Tournament) : Completable {
        return UseCaseHandler.execute(deleteTournamentUseCase, DeleteTournament.RequestValues(tournament)).ignoreElement()
    }

    fun getRoaster(): Single<GetRoaster.ResponseValue> {
        return chosenRoaster?.let {
            Single.just(it)
        } ?: UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { UseCaseHandler.execute(getRoasterUseCase, GetRoaster.RequestValues(it.id, null)) }
                .map {
                    chosenRoaster = it
                    it
                }
    }

    fun createNewLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team ->
                    getRoaster().map { it.players }.flatMap { roaster ->
                        UseCaseHandler.execute(createLineupUseCase, CreateLineup.RequestValues(team.id, tournament, lineupTitle, roaster))
                    }
                }
                .map { it.lineupID }
    }

    fun roasterPlayerStatusChanged(position: Int, status: Boolean) {
        chosenRoaster?.let {
            it.players[position].status = status
            val areSameSize = it.players.filter { it.status }.size == it.players.size
            it.status = when(areSameSize) {
                true -> STATUS_ALL
                false -> STATUS_NONE
            }
        }

    }

    fun showNewRoasterFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROASTER, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROASTER, false).apply()
        }
        return Single.just(show)
    }
}