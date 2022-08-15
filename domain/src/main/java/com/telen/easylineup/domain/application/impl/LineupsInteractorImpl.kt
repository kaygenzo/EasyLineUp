package com.telen.easylineup.domain.application.impl

import android.content.Context
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.LineupsInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.*
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject

internal class LineupsInteractorImpl(private val context: Context) : LineupsInteractor,
    KoinComponent {

    private val playersRepo: PlayerRepository by inject()
    private val lineupsRepo: LineupRepository by inject()
    private val getTeam: GetTeam by inject()
    private val createLineup: CreateLineup by inject()
    private val updateLineupRoster: UpdateLineupRoster by inject()
    private val deleteLineup: DeleteLineup by inject()
    private val saveLineupMode: SaveLineupMode by inject()
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode by inject()
    private val getRoster: GetRoster by inject()
    private val saveBattingOrder: SaveBattingOrder by inject()
    private val getDpAndFlexFromPlayersInField: GetDPAndFlexFromPlayersInField by inject()
    private val saveDpAndFlex: SaveDpAndFlex by inject()
    private val getBatterState: GetBattersState by inject()
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection by inject()
    private val getPlayersInField: GetOnlyPlayersInField by inject()

    private val errors: PublishSubject<DomainErrors.Lineups> = PublishSubject.create()

    override fun insertLineups(lineups: List<Lineup>): Completable {
        return lineupsRepo.insertLineups(lineups)
    }

    override fun getCompleteRoster(): Single<TeamRosterSummary> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                UseCaseHandler.execute(getRoster, GetRoster.RequestValues(it.id, null))
            }
            .map { it.summary }
    }

    override fun getRoster(lineupID: Long): Single<TeamRosterSummary> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap { UseCaseHandler.execute(getRoster, GetRoster.RequestValues(it.id, lineupID)) }
            .map { it.summary }
    }

    override fun updateRoster(lineupID: Long, roster: List<RosterPlayerStatus>): Completable {
        return UseCaseHandler
            .execute(updateLineupRoster, UpdateLineupRoster.RequestValues(lineupID, roster))
            .ignoreElement()
    }

    override fun saveLineup(
        tournament: Tournament,
        lineupTitle: String,
        rosterFilter: TeamRosterSummary,
        lineupEventTime: Long,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Single<Long> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues()).map { it.team }
            .flatMap { team ->
                val request = CreateLineup.RequestValues(
                    team.id,
                    tournament,
                    lineupTitle,
                    lineupEventTime,
                    rosterFilter.players,
                    strategy,
                    extraHittersSize
                )
                UseCaseHandler.execute(createLineup, request)
            }
            .map { it.lineupID }
            .doOnError {
                if (it is LineupNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_LINEUP_NAME)
                } else if (it is TournamentNameEmptyException) {
                    errors.onNext(DomainErrors.Lineups.INVALID_TOURNAMENT_NAME)
                }
            }
    }

    override fun deleteLineup(lineupID: Long?): Completable {
        val requestValues = DeleteLineup.RequestValues(lineupID)
        return UseCaseHandler.execute(deleteLineup, requestValues).ignoreElement()
            .doOnError {
                errors.onNext(DomainErrors.Lineups.DELETE_LINEUP_FAILED)
            }
    }

    override fun updateLineupMode(
        isEnabled: Boolean,
        lineupID: Long?,
        lineupMode: Int,
        list: List<PlayerWithPosition>,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable {
        return UseCaseHandler.execute(
            saveLineupMode,
            SaveLineupMode.RequestValues(lineupID, lineupMode)
        )
            .doOnError {
                errors.onNext(DomainErrors.Lineups.SAVE_LINEUP_MODE_FAILED)
            }
            .flatMapCompletable {
                UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
                    .map { it.team }
                    .flatMapCompletable {
                        val requestValues = UpdatePlayersWithLineupMode.RequestValues(
                            list,
                            isEnabled,
                            it.type,
                            strategy,
                            extraHittersSize
                        )
                        UseCaseHandler.execute(updatePlayersWithLineupMode, requestValues)
                            .ignoreElement()
                    }
            }
    }

    override fun saveBattingOrder(players: List<PlayerWithPosition>): Completable {
        val requestValues = SaveBattingOrder.RequestValues(players)
        return UseCaseHandler.execute(saveBattingOrder, requestValues)
            .ignoreElement()
            .doOnError { errors.onNext(DomainErrors.Lineups.SAVE_BATTING_ORDER_FAILED) }
    }

    override fun observeLineupById(id: Long): LiveData<Lineup> {
        return lineupsRepo.getLineupById(id)
    }

    override fun observeErrors(): Subject<DomainErrors.Lineups> {
        return errors
    }

    override fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>> {
        return playersRepo.getTeamPlayersAndMaybePositions(id)
    }

    override fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = GetDPAndFlexFromPlayersInField.RequestValues(list, it.type)
                UseCaseHandler.execute(getDpAndFlexFromPlayersInField, request)
            }
            .map { it.configResult }
            .doOnError {
                if (it is NeedAssignPitcherFirstException) {
                    errors.onNext(DomainErrors.Lineups.NEED_ASSIGN_PITCHER_FIRST)
                }
            }
    }

    override fun linkDpAndFlex(
        dp: Player?,
        flex: Player?,
        lineupID: Long?,
        list: List<PlayerWithPosition>,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable {
        return UseCaseHandler.execute(
            saveDpAndFlex, SaveDpAndFlex.RequestValues(
                lineupID = lineupID,
                dp = dp,
                flex = flex,
                players = list,
                strategy = strategy,
                extraHittersSize = extraHittersSize
            )
        )
            .ignoreElement()
            .doOnError {
                if (it is NeedAssignBothPlayersException) {
                    errors.onNext(DomainErrors.Lineups.DP_OR_FLEX_NOT_ASSIGNED)
                }
            }
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
        return UseCaseHandler.execute(
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

    override fun getNotSelectedPlayersFromList(
        list: List<PlayerWithPosition>,
        lineupID: Long?,
        sortBy: FieldPosition?
    ): Single<List<Player>> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap { UseCaseHandler.execute(getRoster, GetRoster.RequestValues(it.id, lineupID)) }
            .flatMap {
                val requestValues = GetListAvailablePlayersForSelection.RequestValues(
                    list,
                    sortBy,
                    it.summary.players
                )
                UseCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
            }
            .map { it.players }
            .doOnError { errors.onNext(DomainErrors.Lineups.LIST_AVAILABLE_PLAYERS_EMPTY) }
    }

    override fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<Player>> {
        return UseCaseHandler.execute(getPlayersInField, GetOnlyPlayersInField.RequestValues(list))
            .map { it.playersInField }
            .doOnError {
                errors.onNext(DomainErrors.Lineups.LIST_AVAILABLE_PLAYERS_EMPTY)
            }
    }
}