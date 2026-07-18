/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TournamentWithLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface LineupRepository {
    fun insertLineup(lineup: Lineup): Single<Long>
    fun insertLineups(lineups: List<Lineup>): Completable
    fun updateLineup(lineup: Lineup): Completable
    fun updateLineupsWithRowCount(lineups: List<Lineup>): Single<Int>
    fun deleteLineup(lineup: Lineup): Completable
    fun deleteLineups(lineups: List<Lineup>): Completable
    fun getLineups(): Single<List<Lineup>>
    fun getLineupById(lineupId: Long): Flowable<Lineup>
    fun getLineupByHash(hash: String): Single<Lineup>
    fun getLineupByIdSingle(lineupId: Long): Single<Lineup>
    fun getLineupsForTournamentRx(tournamentId: Long, teamId: Long): Single<List<Lineup>>
    fun getLastLineup(teamId: Long): Maybe<Lineup>
    fun getAllTournamentsWithLineups(
        filter: String,
        teamId: Long
    ): Single<List<TournamentWithLineup>>

    fun getAllPlayerPositionsForTournament(
        tournamentId: Long,
        teamId: Long
    ): Single<List<PlayerInLineup>>
}
