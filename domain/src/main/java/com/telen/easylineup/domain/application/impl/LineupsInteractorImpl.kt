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
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDPAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.UpdateLineup
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class LineupsInteractorImpl(private val context: Context) : LineupsInteractor,
    KoinComponent {

    private val playersRepo: PlayerRepository by inject()
    private val lineupsRepo: LineupRepository by inject()
    private val getTeam: GetTeam by inject()
    private val createLineup: CreateLineup by inject()
    private val updateLineupRoster: UpdateLineupRoster by inject()
    private val deleteLineup: DeleteLineup by inject()
    private val setLineupMode: SetLineupMode by inject()
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode by inject()
    private val getRoster: GetRoster by inject()
    private val saveBattingOrderAndPosition: SaveBattingOrderAndPositions by inject()
    private val getDpAndFlexFromPlayersInField: GetDPAndFlexFromPlayersInField by inject()
    private val saveDpAndFlex: SaveDpAndFlex by inject()
    private val getBatterState: GetBattersState by inject()
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection by inject()
    private val getPlayersInField: GetOnlyPlayersInField by inject()
    private val updateLineup: UpdateLineup by inject()

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
    }

    override fun updateLineupMode(
        isEnabled: Boolean,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable {
        return Completable.defer {
            val lineupMode = if (isEnabled) MODE_ENABLED else MODE_DISABLED
            val request = SetLineupMode.RequestValues(lineup, lineupMode)
            UseCaseHandler.execute(setLineupMode, request)
                .ignoreElement()
                .andThen(UseCaseHandler.execute(getTeam, GetTeam.RequestValues()))
                .map { it.team }
                .flatMap {
                    val update = UpdatePlayersWithLineupMode.RequestValues(list, lineup, it.type)
                    UseCaseHandler.execute(updatePlayersWithLineupMode, update)
                }
                .ignoreElement()
        }
    }

    override fun updateLineup(lineup: Lineup, players: List<PlayerWithPosition>): Completable {
        val requestValues = SaveBattingOrderAndPositions.RequestValues(lineup, players)
        return UseCaseHandler.execute(saveBattingOrderAndPosition, requestValues).ignoreElement()
    }

    override fun updateLineup(lineup: Lineup): Completable {
        return UseCaseHandler.execute(updateLineup, UpdateLineup.RequestValues(lineup))
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
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = GetDPAndFlexFromPlayersInField.RequestValues(list, it.type)
                UseCaseHandler.execute(getDpAndFlexFromPlayersInField, request)
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
        return UseCaseHandler.execute(saveDpAndFlex, request).ignoreElement()
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
        lineup: Lineup,
        sortBy: FieldPosition?
    ): Single<List<PlayerWithPosition>> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                UseCaseHandler.execute(
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
                UseCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
            }
            .map { it.players }
    }

    override fun getPlayersInFieldFromList(list: List<PlayerWithPosition>)
            : Single<List<PlayerWithPosition>> {
        return UseCaseHandler.execute(getPlayersInField, GetOnlyPlayersInField.RequestValues(list))
            .map { it.playersInField }
    }
}