/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.TournamentsInteractor
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MapInfo
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.TournamentStatsUiConfig
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

internal class TournamentsInteractorImpl(
    private val tournamentsRepo: TournamentRepository,
    private val getTeam: GetTeam,
    private val deleteTournamentUseCase: DeleteTournamentLineups,
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineupsUseCase,
    private val getTournamentsUseCase: GetTournaments,
    private val tableDataUseCase: GetTournamentStatsForPositionTable,
    private val saveTournament: SaveTournament,
    private val getTournamentMapLink: GetTournamentMapLink,
    private val useCaseHandler: UseCaseHandler
) : TournamentsInteractor {

    override fun getTournaments(): Single<List<Tournament>> {
        return useCaseHandler.execute(getTournamentsUseCase, GetTournaments.RequestValues())
            .map { it.tournaments }
    }

    override fun observeTournaments(): LiveData<List<Tournament>> {
        return tournamentsRepo.observeTournaments()
    }

    override fun insertTournaments(tournaments: List<Tournament>): Completable {
        return tournamentsRepo.insertTournaments(tournaments)
    }

    override fun deleteTournament(tournament: Tournament): Completable {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                useCaseHandler.execute(
                    deleteTournamentUseCase,
                    DeleteTournamentLineups.RequestValues(tournament, it)
                )
            }
            .ignoreElement()
    }

    override fun getCategorizedLineups(filter: String):
    Single<List<Pair<Tournament, List<Lineup>>>> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .flatMap {
                useCaseHandler.execute(
                    getAllTournamentsWithLineupsUseCase,
                    GetAllTournamentsWithLineupsUseCase.RequestValues(filter, it.team.id)
                )
            }
            .map { it.result }
    }

    override fun getPlayersPositionForTournament(
        tournament: Tournament,
        strategy: TeamStrategy
    ): Single<TournamentStatsUiConfig> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .flatMap {
                val request =
                    GetTournamentStatsForPositionTable.RequestValues(tournament, it.team, strategy)
                useCaseHandler.execute(tableDataUseCase, request)
            }
            .map { it.uiConfig }
    }

    override fun saveTournament(tournament: Tournament): Completable {
        return useCaseHandler.execute(saveTournament, SaveTournament.RequestValues(tournament))
            .ignoreElement()
    }

    override fun getTournamentMapInfo(
        tournament: Tournament,
        apiKey: String?,
        width: Int,
        height: Int
    ): Single<MapInfo> {
        return useCaseHandler.execute(
            getTournamentMapLink,
            GetTournamentMapLink.RequestValues(tournament, apiKey, width, height)
        ).map { it.mapInfo }
    }
}
