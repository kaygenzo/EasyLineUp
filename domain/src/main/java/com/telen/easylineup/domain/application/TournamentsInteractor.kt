/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUiConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TournamentsInteractor {
    fun getTournaments(): Single<List<Tournament>>
    fun observeTournaments(): LiveData<List<Tournament>>

    /** @deprecated
     * @return **/
    fun insertTournaments(tournaments: List<Tournament>): Completable
    fun deleteTournament(tournament: Tournament): Completable
    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>>
    fun getPlayersPositionForTournament(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUiConfig>

    fun saveTournament(tournament: Tournament): Completable
    fun getTournamentMapInfo(
        tournament: Tournament,
        apiKey: String?,
        width: Int,
        height: Int
    ): Single<MapInfo>
}
