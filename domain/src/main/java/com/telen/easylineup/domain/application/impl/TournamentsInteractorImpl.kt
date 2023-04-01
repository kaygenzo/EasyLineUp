package com.telen.easylineup.domain.application.impl

import android.content.Context
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.TournamentsInteractor
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUIConfig
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class TournamentsInteractorImpl(private val context: Context) : TournamentsInteractor,
    KoinComponent {

    private val tournamentsRepo: TournamentRepository by inject()
    private val getTeam: GetTeam by inject()
    private val deleteTournamentUseCase: DeleteTournamentLineups by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineupsUseCase by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val tableDataUseCase: GetTournamentStatsForPositionTable by inject()

    override fun getTournaments(): Single<List<Tournament>> {
        return UseCaseHandler.execute(getTournamentsUseCase, GetTournaments.RequestValues())
            .map { it.tournaments }
    }

    override fun insertTournaments(tournaments: List<Tournament>): Completable {
        return tournamentsRepo.insertTournaments(tournaments)
    }

    override fun deleteTournament(tournament: Tournament): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                UseCaseHandler.execute(
                    deleteTournamentUseCase,
                    DeleteTournamentLineups.RequestValues(tournament, it)
                )
            }
            .ignoreElement()
    }

    override fun getCategorizedLineups(filter: String):
            Single<List<Pair<Tournament, List<Lineup>>>> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .flatMap {
                UseCaseHandler.execute(
                    getAllTournamentsWithLineupsUseCase,
                    GetAllTournamentsWithLineupsUseCase.RequestValues(filter, it.team.id)
                )
            }
            .map { it.result }
    }

    override fun getPlayersPositionForTournament(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUIConfig> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .flatMap {
                val request = GetTournamentStatsForPositionTable.RequestValues(
                    tournament,
                    it.team,
                    context,
                    strategy
                )
                UseCaseHandler.execute(tableDataUseCase, request)
            }
            .map { it.uiConfig }
    }
}