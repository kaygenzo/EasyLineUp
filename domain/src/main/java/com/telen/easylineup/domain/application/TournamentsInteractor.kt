package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TournamentsInteractor {
    fun getTournaments(): Single<List<Tournament>>
    fun observeTournaments(): LiveData<List<Tournament>>

    /** @deprecated **/
    fun insertTournaments(tournaments: List<Tournament>): Completable
    fun deleteTournament(tournament: Tournament): Completable
    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>>
    fun getPlayersPositionForTournament(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUIConfig>

    fun saveTournament(tournament: Tournament): Completable
    fun getTournamentMapInfo(
        tournament: Tournament,
        apiKey: String?,
        width: Int,
        height: Int
    ): Single<MapInfo>
}