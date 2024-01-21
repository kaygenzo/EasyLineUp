/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import android.location.Geocoder
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.application.ApplicationInteractorImpl
import com.telen.easylineup.domain.application.DataInteractor
import com.telen.easylineup.domain.application.LineupsInteractor
import com.telen.easylineup.domain.application.PlayerFieldPositionsInteractor
import com.telen.easylineup.domain.application.PlayersInteractor
import com.telen.easylineup.domain.application.TeamsInteractor
import com.telen.easylineup.domain.application.TournamentsInteractor
import com.telen.easylineup.domain.application.impl.DataInteractorImpl
import com.telen.easylineup.domain.application.impl.LineupsInteractorImpl
import com.telen.easylineup.domain.application.impl.PlayerFieldPositionsInteractorImpl
import com.telen.easylineup.domain.application.impl.PlayersInteractorImpl
import com.telen.easylineup.domain.application.impl.TeamsInteractorImpl
import com.telen.easylineup.domain.application.impl.TournamentsInteractorImpl
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
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.domain.usecases.GetTournaments
import com.telen.easylineup.domain.usecases.ImportData
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
        single { GetDpAndFlexFromPlayersInField() }
        single { SaveDpAndFlex() }
        single { SavePlayerNumberOverlay(get()) }
        single { GetShirtNumberHistory(get()) }
        single { ValidatorUtils() }
        single { GetBattersState() }
        single { UpdateLineup(get()) }
        single { UpdatePlayersWithBatters() }
        single { SaveTournament(get()) }
        single { GetTournamentMapLink(get()) }
        factory { Geocoder(get()) }
    }
}
