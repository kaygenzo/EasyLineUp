/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import android.content.Context
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.LineupsInteractor
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNullException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class LineupsInteractorImpl(
    private val context: Context,
    private val playersRepo: PlayerRepository,
    private val lineupsRepo: LineupRepository,
    private val getTeam: GetTeam,
    private val createLineup: CreateLineup,
    private val updateLineupRoster: UpdateLineupRoster,
    private val deleteLineup: DeleteLineup,
    private val setLineupMode: SetLineupMode,
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode,
    private val getRoster: GetRoster,
    private val saveBattingOrderAndPosition: SaveBattingOrderAndPositions,
    private val getDpAndFlexFromPlayersInField: GetDpAndFlexFromPlayersInField,
    private val saveDpAndFlex: SaveDpAndFlex,
    private val getBatterState: GetBattersState,
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection,
    private val getPlayersInField: GetOnlyPlayersInField,
    private val updateLineup: UpdateLineup,
    private val updatePlayersWithBatters: UpdatePlayersWithBatters,
    private val useCaseHandler: UseCaseHandler
) : LineupsInteractor {

    private val errors: PublishSubject<DomainErrors.Lineups> = PublishSubject.create()

    override fun insertLineups(lineups: List<Lineup>): Completable {
        return lineupsRepo.insertLineups(lineups)
    }

    override fun getCompleteRoster(): Single<TeamRosterSummary> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                useCaseHandler.execute(getRoster, GetRoster.RequestValues(it.id, null))
            }
            .map { it.summary }
    }

    override fun getRoster(lineupId: Long): Single<TeamRosterSummary> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap { useCaseHandler.execute(getRoster, GetRoster.RequestValues(it.id, lineupId)) }
            .map { it.summary }
    }

    override fun updateRoster(lineupId: Long, roster: List<RosterPlayerStatus>): Completable {
        return useCaseHandler
            .execute(updateLineupRoster, UpdateLineupRoster.RequestValues(lineupId, roster))
            .ignoreElement()
    }

    override fun saveLineup(lineup: Lineup, rosterFilter: TeamRosterSummary): Single<Lineup> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues()).map { it.team }
            .flatMap { team ->
                val request = CreateLineup.RequestValues(team.id, lineup, rosterFilter.players)
                useCaseHandler.execute(createLineup, request)
            }
            .map { it.lineup }
            .doOnError {
                if (it is LineupNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_LINEUP_NAME)
                } else if (it is TournamentNameEmptyException || it is TournamentNullException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_TOURNAMENT_NAME)
                }
            }
    }

    override fun deleteLineup(lineupId: Long?): Completable {
        val requestValues = DeleteLineup.RequestValues(lineupId)
        return useCaseHandler.execute(deleteLineup, requestValues).ignoreElement()
    }

    override fun updateLineupMode(
        isEnabled: Boolean,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable {
        return Completable.defer {
            val lineupMode = if (isEnabled) MODE_ENABLED else MODE_DISABLED
            val request = SetLineupMode.RequestValues(lineup, lineupMode)
            useCaseHandler.execute(setLineupMode, request)
                .ignoreElement()
                .andThen(useCaseHandler.execute(getTeam, GetTeam.RequestValues()))
                .map { it.team }
                .flatMap {
                    val update = UpdatePlayersWithLineupMode.RequestValues(list, lineup, it.type)
                    useCaseHandler.execute(updatePlayersWithLineupMode, update)
                }
                .ignoreElement()
        }
    }

    override fun updateLineupAndPlayers(lineup: Lineup, players: List<PlayerWithPosition>): Completable {
        val requestValues = SaveBattingOrderAndPositions.RequestValues(lineup, players)
        return useCaseHandler.execute(saveBattingOrderAndPosition, requestValues).ignoreElement()
    }

    override fun updateLineup(lineup: Lineup): Completable {
        return useCaseHandler.execute(updateLineup, UpdateLineup.RequestValues(lineup))
            .ignoreElement()
    }

    override fun observeLineupById(id: Long): LiveData<Lineup> {
        return lineupsRepo.getLineupById(id)
    }

    override fun getLineupById(id: Long): Single<Lineup> {
        return lineupsRepo.getLineupByIdSingle(id)
    }

    override fun observeErrors(): Subject<DomainErrors.Lineups> {
        return errors
    }

    override fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>> {
        return playersRepo.getTeamPlayersAndMaybePositions(id)
    }

    override fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = GetDpAndFlexFromPlayersInField.RequestValues(list, it.type)
                useCaseHandler.execute(getDpAndFlexFromPlayersInField, request)
            }
            .map { it.configResult }
    }

    override fun linkDpAndFlex(
        dp: Player?,
        flex: Player?,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable {
        val request =
            SaveDpAndFlex.RequestValues(lineup = lineup, dp = dp, flex = flex, players = list)
        return useCaseHandler.execute(saveDpAndFlex, request).ignoreElement()
    }

    override fun getBatterStates(
        players: List<PlayerWithPosition>,
        teamType: Int,
        batterSize: Int,
        extraHitterSize: Int,
        lineupMode: Int,
        isDebug: Boolean,
        isEditable: Boolean
    ): Single<List<BatterState>> {
        return useCaseHandler.execute(
            getBatterState, GetBattersState.RequestValues(
                context = context,
                players = players,
                teamType = teamType,
                batterSize = batterSize,
                extraHitterSize = extraHitterSize,
                isDebug = isDebug,
                isEditable = isEditable
            )
        ).map { it.players }
    }

    override fun updatePlayersWithBatters(
        players: List<PlayerWithPosition>,
        batters: List<BatterState>
    ): Completable {
        return useCaseHandler.execute(
            updatePlayersWithBatters, UpdatePlayersWithBatters.RequestValues(
                players = players,
                batters = batters
            )
        ).ignoreElement()
    }

    override fun getNotSelectedPlayersFromList(
        list: List<PlayerWithPosition>,
        lineup: Lineup,
        sortBy: FieldPosition?
    ): Single<List<PlayerWithPosition>> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                useCaseHandler.execute(
                    getRoster,
                    GetRoster.RequestValues(it.id, lineup.id)
                )
            }
            .flatMap {
                val requestValues = GetListAvailablePlayersForSelection.RequestValues(
                    list,
                    sortBy,
                    it.summary.players
                )
                useCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
            }
            .map { it.players }
    }

    override fun getPlayersInFieldFromList(list: List<PlayerWithPosition>)
    : Single<List<PlayerWithPosition>> {
        return useCaseHandler.execute(getPlayersInField, GetOnlyPlayersInField.RequestValues(list))
            .map { it.playersInField }
    }
}
