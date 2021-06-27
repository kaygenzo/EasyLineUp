package com.telen.easylineup.domain

import com.telen.easylineup.domain.application.ApplicationAdapter
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.usecases.*
import com.telen.easylineup.domain.utils.ValidatorUtils

import org.koin.dsl.module

object DomainModule {
    val domainModules = module {
        single<ApplicationPort> { ApplicationAdapter() }

        single { GetTeam(get()) }
        single { GetAllTeams(get()) }
        single { SaveCurrentTeam(get()) }
        single { GetDashboardTiles(get(), get(), get(), get()) }
        single { SaveDashboardTiles(get()) }
        single { CreateDashboardTiles(get()) }
        single { CreateLineup(get(), get()) }
        single { GetTournaments(get()) }
        single { GetAllTournamentsWithLineups(get()) }
        single { DeleteTournamentLineups(get()) }
        single { GetPlayer(get()) }
        single { DeletePlayer(get()) }
        single { SavePlayer(get()) }
        single { GetPositionsSummaryForPlayer(get()) }
        single { GetPlayers(get()) }
        single { SaveTeam(get()) }
        single { CheckTeam() }
        single { GetTeamCreationNextStep() }
        single { GetTeamCreationPreviousStep() }
        single { SavePlayerFieldPosition(get()) }
        single { DeletePlayerFieldPosition(get()) }
        single { GetListAvailablePlayersForSelection() }
        single { SaveBattingOrder(get()) }
        single { DeleteLineup(get()) }
        single { SaveLineupMode(get()) }
        single { UpdatePlayersWithLineupMode(get()) }
        single { GetRoster(get(), get()) }
        single { UpdateLineupRoster(get()) }
        single { DeleteTeam(get()) }
        single { SwitchPlayersPosition(get()) }
        single { DeleteAllData(get(), get()) }
        single { GetTournamentStatsForPositionTable(get()) }
        single { CheckHashData(get(), get(), get(), get(), get()) }
        single { ExportData(get(), get(), get(), get(), get()) }
        single { ImportData(get(), get(), get(), get(), get()) }
        single { GetOnlyPlayersInField() }
        single { GetDPAndFlexFromPlayersInField() }
        single { SaveDpAndFlex(get()) }
        single { SavePlayerNumberOverlay(get()) }
        single { GetShirtNumberHistory(get()) }
        single { ValidatorUtils() }
        single { GetBattersState() }
    }
}