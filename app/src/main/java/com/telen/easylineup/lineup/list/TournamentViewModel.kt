package com.telen.easylineup.lineup.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.repository.data.FieldPosition
import com.telen.easylineup.repository.data.Lineup
import com.telen.easylineup.repository.data.Tournament
import io.reactivex.Completable

class TournamentViewModel: ViewModel() {

    private val filterLiveData: MutableLiveData<String> = MutableLiveData()

    fun setFilter(filter: String) {
        filterLiveData.value = filter
    }

    fun getTournaments(): LiveData<List<Tournament>>{
        return App.database.tournamentDao().getTournaments()
    }

    fun registerTournamentsChanges(): LiveData<List<Pair<Tournament, List<Lineup>>>> {

        val tournamentsAndLineupsMap = Transformations.switchMap(filterLiveData) {
            getCategorizedLineups(it)
        }

        return Transformations.map(tournamentsAndLineupsMap) {
            val list = mutableListOf<Pair<Tournament, List<Lineup>>>()
            it.forEach { item ->
                val tournament = item.key
                val lineups = item.value
                list.add(Pair(tournament, lineups))
            }
            list
        }
    }

    private fun getCategorizedLineups(filter: String): LiveData<Map<Tournament, List<Lineup>>> {
         return Transformations.map(App.database.lineupDao().getAllTournamentsWithLineups(filter)) {
            val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
            val lineups: MutableMap<Long, Lineup> = mutableMapOf()

            it.forEach { item ->
                val tournament = item.toTournament()
                val lineup = item.toLineup()
                val position = FieldPosition.getFieldPosition(item.position)

                if(result[tournament] == null) {
                    result[tournament] = mutableListOf()
                }

                if(lineup.id > 0) {
                    if(lineups[lineup.id]==null) {
                        lineups[lineup.id] = lineup
                        result[tournament]?.add(lineup)
                    }
                    if(item.fieldPositionID > 0)
                        position?.let {
                            lineups[lineup.id]?.playerPositions?.add(position)
                        }
                }
            }
            result
        }
    }

    fun deleteTournament(tournament: Tournament) : Completable {
        return App.database.tournamentDao().deleteTournament(tournament)
    }
}