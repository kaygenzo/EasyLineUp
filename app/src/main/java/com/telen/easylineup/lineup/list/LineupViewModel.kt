package com.telen.easylineup.lineup.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

sealed class SaveResult
data class InvalidLineupName(val errorRes: Int): SaveResult()
data class InvalidTournamentName(val errorRes: Int): SaveResult()
data class SaveSuccess(val lineupID: Long, val lineupName: String): SaveResult()

class LineupViewModel: ViewModel(), KoinComponent {

    private val filterLiveData: MutableLiveData<String> = MutableLiveData()
    private var chosenRoster: GetRoster.ResponseValue? = null

    private val saveResult = MutableLiveData<SaveResult>()

    private val createLineupUseCase: CreateLineup by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val deleteTournamentUseCase: DeleteTournament by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineups by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val getRosterUseCase: GetRoster by inject()

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun registerFilterChanged() : LiveData<String> {
        return filterLiveData
    }

    fun registerSaveResults(): LiveData<SaveResult> {
        return saveResult
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

    fun getRoster(): Single<GetRoster.ResponseValue> {
        return chosenRoster?.let {
            Single.just(it)
        } ?: UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { UseCaseHandler.execute(getRosterUseCase, GetRoster.RequestValues(it.id, null)) }
                .map {
                    chosenRoster = it
                    it
                }
    }

    fun saveLineup(tournament: Tournament, lineupTitle: String) {
        UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team ->
                    getRoster().map { it.players }.flatMap { roster ->
                        UseCaseHandler.execute(createLineupUseCase, CreateLineup.RequestValues(team.id, tournament, lineupTitle, roster))
                    }
                }
                .map { it.lineupID }
                .subscribe({
                    saveResult.value = SaveSuccess(it, lineupTitle)
                }, {
                    if (it is LineupNameEmptyException) {
                        saveResult.value = InvalidLineupName(R.string.lineup_creation_error_name_empty)
                    }
                    else if(it is TournamentNameEmptyException) {
                        saveResult.value = InvalidTournamentName(R.string.lineup_creation_error_tournament_empty)
                    }
                })
    }

    fun rosterPlayerStatusChanged(position: Int, status: Boolean) {
        chosenRoster?.let {
            it.players[position].status = status
            val areSameSize = it.players.filter { it.status }.size == it.players.size
            it.status = when(areSameSize) {
                true -> Constants.STATUS_ALL
                false -> Constants.STATUS_PARTIAL
            }
        }

    }

    fun showNewRosterFeature(context: Context): Single<Boolean> {
        val prefs = context.getSharedPreferences(Constants.APPLICATION_PREFERENCES, 0)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, true)
        if(show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_NEW_ROSTER, false).apply()
        }
        return Single.just(show)
    }
}