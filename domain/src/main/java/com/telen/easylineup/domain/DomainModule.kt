package com.telen.easylineup.domain

import com.telen.easylineup.domain.application.ApplicationAdapter
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.usecases.*

import org.koin.dsl.module

object DomainModule {
    val domainModules = module {
        single<ApplicationPort> { ApplicationAdapter() }

        single { GetTeam(get()) }
        single { GetAllTeams(get()) }
        single { SaveCurrentTeam(get()) }
        single { GetTeamSize(get()) }
        single { GetMostUsedPlayer(get(), get()) }
        single { CreateLineup(get(), get()) }
        single { GetTournaments(get()) }
        single { GetAllTournamentsWithLineups(get()) }
        single { DeleteTournament(get()) }
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
    }
}