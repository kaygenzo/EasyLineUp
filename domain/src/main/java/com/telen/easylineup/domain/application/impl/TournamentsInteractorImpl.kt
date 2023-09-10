package com.telen.easylineup.domain.application.impl

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.TournamentsInteractor
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUIConfig
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineupsUseCase
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.SaveTournament
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class TournamentsInteractorImpl : TournamentsInteractor, KoinComponent {

    private val tournamentsRepo: TournamentRepository by inject()
    private val getTeam: GetTeam by inject()
    private val deleteTournamentUseCase: DeleteTournamentLineups by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineupsUseCase by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val tableDataUseCase: GetTournamentStatsForPositionTable by inject()
    private val saveTournament: SaveTournament by inject()
    private val getTournamentMapLink: GetTournamentMapLink by inject()

    override fun getTournaments(): Single<List<Tournament>> {
        return UseCaseHandler.execute(getTournamentsUseCase, GetTournaments.RequestValues())
            .map { it.tournaments }
    }

    override fun observeTournaments(): LiveData<List<Tournament>> {
        return tournamentsRepo.observeTournaments()
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
                val request =
                    GetTournamentStatsForPositionTable.RequestValues(tournament, it.team, strategy)
                UseCaseHandler.execute(tableDataUseCase, request)
            }
            .map { it.uiConfig }
    }

    override fun saveTournament(tournament: Tournament): Completable {
        return UseCaseHandler.execute(saveTournament, SaveTournament.RequestValues(tournament))
            .ignoreElement()
    }

    override fun getTournamentMapInfo(
        tournament: Tournament,
        apiKey: String?,
        width: Int,
        height: Int
    ): Single<MapInfo> {
        return UseCaseHandler.execute(
            getTournamentMapLink,
            GetTournamentMapLink.RequestValues(tournament, apiKey, width, height)
        ).map { it.mapInfo }
    }
}