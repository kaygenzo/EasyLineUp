package com.telen.easylineup.application

import com.telen.easylineup.domain.*
import org.koin.dsl.module

// just declare it
val appModules = module {
    //dao
    single { App.database.teamDao() }
    single { App.database.tournamentDao() }
    single { App.database.playerDao() }
    single { App.database.playerFieldPositionsDao() }
    single { App.database.lineupDao() }

    //use cases
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
    single { GetRoaster(get(), get()) }
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