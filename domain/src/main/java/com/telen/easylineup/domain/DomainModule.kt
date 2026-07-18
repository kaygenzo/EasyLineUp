/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.mock.DatabaseMockProvider
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.CheckHashData
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.CreateDashboardTiles
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineupsUseCase
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTeamEmails
import com.telen.easylineup.domain.usecases.GetTeamPhones
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.InsertLineups
import com.telen.easylineup.domain.usecases.InsertPlayerFieldPositions
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
import com.telen.easylineup.domain.usecases.InsertPlayers
import com.telen.easylineup.domain.usecases.InsertTeam
import com.telen.easylineup.domain.usecases.InsertTournaments
import com.telen.easylineup.domain.usecases.ObserveLineupById
import com.telen.easylineup.domain.usecases.ObservePlayer
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import com.telen.easylineup.domain.usecases.ObservePlayers
import com.telen.easylineup.domain.usecases.ObserveTeamPlayersAndMaybePositionsForLineup
import com.telen.easylineup.domain.usecases.ObserveTeams
import com.telen.easylineup.domain.usecases.ObserveTournaments
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.domain.usecases.SaveDashboardTiles
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.SaveTournament
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
import com.telen.easylineup.domain.utils.ValidatorUtils
import org.koin.dsl.module

object DomainModule {
    val domainModules = module {
        single {
            DatabaseMockProvider(
                insertTeamUseCase = get(),
                insertPlayersUseCase = get(),
                insertPlayerNumberOverlaysUseCase = get(),
                insertLineupsUseCase = get(),
                insertPlayerFieldPositionsUseCase = get(),
                insertTournamentsUseCase = get()
            )
        }
        single { GetTeam(get(), get()) }
        single { GetAllTeams(get(), get()) }
        single { SaveCurrentTeam(get(), get()) }
        single { GetDashboardTiles(get(), get(), get(), get(), get(), get(), get()) }
        single { SaveDashboardTiles(get(), get()) }
        single { CreateDashboardTiles(get(), get()) }
        single { CreateLineup(get(), get(), get()) }
        single { GetTournaments(get(), get()) }
        single { GetAllTournamentsWithLineupsUseCase(get(), get(), get()) }
        single { DeleteTournamentLineups(get(), get(), get()) }
        single { GetPlayer(get(), get()) }
        single { DeletePlayer(get(), get(), get()) }
        single { SavePlayer(get(), get(), get(), get()) }
        single { GetPositionsSummaryForPlayer(get(), get()) }
        single { GetPlayers(get(), get(), get()) }
        single { GetTeamEmails(get(), get()) }
        single { GetTeamPhones(get(), get()) }
        single { ObservePlayer(get()) }
        single { ObservePlayers(get()) }
        single { ObservePlayerNumberOverlays(get()) }
        single { InsertPlayers(get(), get()) }
        single { InsertPlayerNumberOverlays(get(), get()) }
        single { SaveTeam(get(), get(), get(), get()) }
        single { CheckTeam(get()) }
        single { ObserveTeams(get()) }
        single { InsertTeam(get(), get()) }
        single { AssignPlayerFieldPosition(get(), get()) }
        single { DeletePlayerFieldPosition(get()) }
        single { InsertPlayerFieldPositions(get(), get()) }
        single { GetListAvailablePlayersForSelection(get(), get()) }
        single { SaveBattingOrderAndPositions(get(), get(), get()) }
        single { DeleteLineup(get(), get()) }
        single { SetLineupMode(get(), get(), get()) }
        single { UpdatePlayersWithLineupMode(get()) }
        single { GetRoster(get(), get(), get(), get()) }
        single { UpdateLineupRoster(get(), get()) }
        single { DeleteTeam(get(), get()) }
        single { SwitchPlayersPosition(get(), get()) }
        single { DeleteAllData(get(), get(), get()) }
        single { GetTournamentStatsForPositionTable(get(), get(), get(), get()) }
        single { CheckHashData(get(), get(), get(), get(), get(), get()) }
        single { ExportData(get(), get(), get(), get(), get(), get(), get()) }
        single { ImportData(get(), get(), get(), get(), get(), get()) }
        single { GetOnlyPlayersInField(get()) }
        single { GetDpAndFlexFromPlayersInField(get(), get()) }
        single { SaveDpAndFlex(get()) }
        single { SavePlayerNumberOverlay(get(), get()) }
        single { GetShirtNumberHistory(get(), get(), get()) }
        single { ValidatorUtils(get()) }
        single { GetBattersState(get(), get()) }
        single { UpdateLineup(get(), get()) }
        single { UpdatePlayersWithBatters(get()) }
        single { SaveTournament(get(), get()) }
        single { GetTournamentMapLink(get(), get()) }
        single { ObserveTournaments(get()) }
        single { InsertTournaments(get(), get()) }
        single { InsertLineups(get(), get()) }
        single { ObserveLineupById(get()) }
        single { GetLineupById(get(), get()) }
        single { ObserveTeamPlayersAndMaybePositionsForLineup(get()) }
    }
}
