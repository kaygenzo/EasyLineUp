package com.telen.easylineup.domain.application

import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single

interface TournamentsInteractor {
    fun getTournaments(): Single<List<Tournament>>

    /** @deprecated **/
    fun insertTournaments(tournaments: List<Tournament>): Completable
    fun deleteTournament(tournament: Tournament): Completable
    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>>
    fun getPlayersPositionForTournament(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUIConfig>
}