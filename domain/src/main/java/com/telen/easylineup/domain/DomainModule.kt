package com.telen.easylineup.domain

import android.location.Geocoder
import com.telen.easylineup.domain.application.*
import com.telen.easylineup.domain.application.impl.*
import com.telen.easylineup.domain.usecases.*
import com.telen.easylineup.domain.utils.ValidatorUtils
import org.koin.dsl.module

object DomainModule {
    val domainModules = module {
        single<ApplicationInteractor> { ApplicationInteractorImpl() }
        single<PlayersInteractor> { PlayersInteractorImpl() }
        single<TeamsInteractor> { TeamsInteractorImpl() }
        single<LineupsInteractor> { LineupsInteractorImpl(get()) }
        single<TournamentsInteractor> { TournamentsInteractorImpl() }
        single<PlayerFieldPositionsInteractor> { PlayerFieldPositionsInteractorImpl() }
        single<DataInteractor> { DataInteractorImpl(get()) }

        single { GetTeam(get()) }
        single { GetAllTeams(get()) }
        single { SaveCurrentTeam(get()) }
        single { GetDashboardTiles(get(), get(), get(), get()) }
        single { SaveDashboardTiles(get()) }
        single { CreateDashboardTiles(get()) }
        single { CreateLineup(get()) }
        single { GetTournaments(get()) }
        single { GetAllTournamentsWithLineupsUseCase(get()) }
        single { DeleteTournamentLineups(get()) }
        single { GetPlayer(get()) }
        single { DeletePlayer(get()) }
        single { SavePlayer(get()) }
        single { GetPositionsSummaryForPlayer(get()) }
        single { GetPlayers(get()) }
        single { SaveTeam(get()) }
        single { CheckTeam() }
        single { AssignPlayerFieldPosition() }
        single { DeletePlayerFieldPosition() }
        single { GetListAvailablePlayersForSelection() }
        single { SaveBattingOrderAndPositions(get(), get()) }
        single { DeleteLineup(get()) }
        single { SetLineupMode() }
        single { UpdatePlayersWithLineupMode() }
        single { GetRoster(get(), get()) }
        single { UpdateLineupRoster(get()) }
        single { DeleteTeam(get()) }
        single { SwitchPlayersPosition() }
        single { DeleteAllData(get(), get()) }
        single { GetTournamentStatsForPositionTable(get(), get()) }
        single { CheckHashData(get(), get(), get(), get(), get()) }
        single { ExportData(get(), get(), get(), get(), get()) }
        single { ImportData(get(), get(), get(), get(), get()) }
        single { GetOnlyPlayersInField() }
        single { GetDPAndFlexFromPlayersInField() }
        single { SaveDpAndFlex() }
        single { SavePlayerNumberOverlay(get()) }
        single { GetShirtNumberHistory(get()) }
        single { ValidatorUtils() }
        single { GetBattersState() }
        single { UpdateLineup(get()) }
        single { UpdatePlayersWithBatters()}
        single { SaveTournament(get()) }
        single { GetTournamentMapLink(get()) }
        factory { Geocoder(get()) }
    }
}