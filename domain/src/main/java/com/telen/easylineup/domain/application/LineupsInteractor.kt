package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

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
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable

    fun updateLineup(lineup: Lineup, players: List<PlayerWithPosition>): Completable
    fun observeLineupById(id: Long): LiveData<Lineup>
    fun getLineupById(id: Long): Single<Lineup>
    fun observeErrors(): Subject<DomainErrors.Lineups>
    fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>>
    fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>):
            Single<DpAndFlexConfiguration>

    fun linkDpAndFlex(
        dp: Player?,
        flex: Player?,
        lineup: Lineup,
        list: List<PlayerWithPosition>
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
        lineup: Lineup,
        sortBy: FieldPosition? = null
    ): Single<List<PlayerWithPosition>>

    fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<PlayerWithPosition>>
}