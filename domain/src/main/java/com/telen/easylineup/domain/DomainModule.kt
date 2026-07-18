/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import android.location.Geocoder
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
        single { UseCaseHandler(get()) }
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
        single { GetTeam(get()) }
        single { GetAllTeams(get()) }
        single { SaveCurrentTeam(get()) }
        single { GetDashboardTiles(get(), get(), get(), get(), get(), get()) }
        single { SaveDashboardTiles(get()) }
        single { CreateDashboardTiles(get()) }
        single { CreateLineup(get(), get()) }
        single { GetTournaments(get()) }
        single { GetAllTournamentsWithLineupsUseCase(get(), get()) }
        single { DeleteTournamentLineups(get(), get()) }
        single { GetPlayer(get()) }
        single { DeletePlayer(get(), get()) }
        single { SavePlayer(get(), get(), get()) }
        single { GetPositionsSummaryForPlayer(get()) }
        single { GetPlayers(get(), get()) }
        single { GetTeamEmails(get()) }
        single { GetTeamPhones(get()) }
        single { ObservePlayer(get()) }
        single { ObservePlayers(get()) }
        single { ObservePlayerNumberOverlays(get()) }
        single { InsertPlayers(get()) }
        single { InsertPlayerNumberOverlays(get()) }
        single { SaveTeam(get(), get(), get()) }
        single { CheckTeam() }
        single { ObserveTeams(get()) }
        single { InsertTeam(get()) }
        single { AssignPlayerFieldPosition(get()) }
        single { DeletePlayerFieldPosition() }
        single { InsertPlayerFieldPositions(get()) }
        single { GetListAvailablePlayersForSelection(get()) }
        single { SaveBattingOrderAndPositions(get(), get()) }
        single { DeleteLineup(get()) }
        single { SetLineupMode(get(), get()) }
        single { UpdatePlayersWithLineupMode() }
        single { GetRoster(get(), get(), get()) }
        single { UpdateLineupRoster(get()) }
        single { DeleteTeam(get()) }
        single { SwitchPlayersPosition(get()) }
        single { DeleteAllData(get(), get()) }
        single { GetTournamentStatsForPositionTable(get(), get(), get()) }
        single { CheckHashData(get(), get(), get(), get(), get()) }
        single { ExportData(get(), get(), get(), get(), get(), get()) }
        single { ImportData(get(), get(), get(), get(), get()) }
        single { GetOnlyPlayersInField() }
        single { GetDpAndFlexFromPlayersInField(get()) }
        single { SaveDpAndFlex() }
        single { SavePlayerNumberOverlay(get()) }
        single { GetShirtNumberHistory(get(), get()) }
        single { ValidatorUtils() }
        single { GetBattersState() }
        single { UpdateLineup(get()) }
        single { UpdatePlayersWithBatters() }
        single { SaveTournament(get()) }
        single { GetTournamentMapLink(get()) }
        single { ObserveTournaments(get()) }
        single { InsertTournaments(get()) }
        single { InsertLineups(get()) }
        single { ObserveLineupById(get()) }
        single { GetLineupById(get()) }
        single { ObserveTeamPlayersAndMaybePositionsForLineup(get()) }
        factory { Geocoder(get()) }
    }
}
