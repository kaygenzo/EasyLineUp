/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamRosterSummary
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

interface LineupsInteractor {
    /** @deprecated
     * @return **/
    fun insertLineups(lineups: List<Lineup>): Completable
    fun getCompleteRoster(): Single<TeamRosterSummary>
    fun getRoster(lineupId: Long): Single<TeamRosterSummary>
    fun updateRoster(lineupId: Long, roster: List<RosterPlayerStatus>): Completable
    fun saveLineup(lineup: Lineup, rosterFilter: TeamRosterSummary): Single<Lineup>
    fun deleteLineup(lineupId: Long?): Completable
    fun updateLineupMode(
        isEnabled: Boolean,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable

    fun updateLineupAndPlayers(lineup: Lineup, players: List<PlayerWithPosition>): Completable
    fun updateLineup(lineup: Lineup): Completable
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

    fun updatePlayersWithBatters(
        players: List<PlayerWithPosition>,
        batters: List<BatterState>
    ): Completable

    fun getNotSelectedPlayersFromList(
        list: List<PlayerWithPosition>,
        lineup: Lineup,
        sortBy: FieldPosition? = null
    ): Single<List<PlayerWithPosition>>

    fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<PlayerWithPosition>>
}
