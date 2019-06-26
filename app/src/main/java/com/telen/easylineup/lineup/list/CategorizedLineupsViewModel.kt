package com.telen.easylineup.lineup.list

import android.graphics.PointF
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Tournament

class CategorizedLineupsViewModel: ViewModel() {

    fun getTournaments(): LiveData<List<Tournament>>{
        return App.database.tournamentDao().getTournaments()
    }

    fun getCategorizedLineups(): LiveData<Map<Tournament, List<Lineup>>> {
         return Transformations.map(App.database.lineupDao().getAllTournamentsWithLineups()) {
            val result: MutableMap<Tournament, MutableList<Lineup>> = mutableMapOf()
            val lineups: MutableMap<Long, Lineup> = mutableMapOf()

            it.forEach { item ->
                val tournament = item.toTournament()
                val lineup = item.toLineup()
                val point = PointF(item.x, item.y)

                if(result[tournament] == null) {
                    result[tournament] = mutableListOf()
                }

                if(lineup.id > 0) {
                    if(lineups[lineup.id]==null) {
                        lineups[lineup.id] = lineup
                        result[tournament]?.add(lineup)
                    }
                    if(item.fieldPositionID > 0)
                        lineups[lineup.id]?.playerPositions?.add(point)
                }
            }
            result
        }
    }
}