package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.Subject

interface LineupsInteractor {
    /** @deprecated **/
    fun insertLineups(lineups: List<Lineup>): Completable
    fun getCompleteRoster(): Single<TeamRosterSummary>
    fun getRoster(lineupID: Long): Single<TeamRosterSummary>
    fun updateRoster(lineupID: Long, roster: List<RosterPlayerStatus>): Completable
    fun saveLineup(
        tournament: Tournament,
        lineupTitle: String,
        rosterFilter: TeamRosterSummary,
        lineupEventTime: Long,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Single<Long>

    fun deleteLineup(lineupID: Long?): Completable
    fun updateLineupMode(
        isEnabled: Boolean,
        lineupID: Long?,
        lineupMode: Int,
        list: List<PlayerWithPosition>,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable

    fun saveBattingOrder(players: List<PlayerWithPosition>): Completable
    fun observeLineupById(id: Long): LiveData<Lineup>
    fun observeErrors(): Subject<DomainErrors.Lineups>
    fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>>
    fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration>
    fun linkDpAndFlex(
        dp: Player?,
        flex: Player?,
        lineupID: Long?,
        list: List<PlayerWithPosition>,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable

    fun getBatterStates(
        players: List<PlayerWithPosition>,
        teamType: Int,
        batterSize: Int,
        extraHitterSize: Int,
        lineupMode: Int,
        isDebug: Boolean,
        isEditable: Boolean
    ): Single<List<BatterState>>

    fun getNotSelectedPlayersFromList(
        list: List<PlayerWithPosition>,
        lineupID: Long?,
        sortBy: FieldPosition? = null
    ): Single<List<Player>>

    fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<Player>>
}