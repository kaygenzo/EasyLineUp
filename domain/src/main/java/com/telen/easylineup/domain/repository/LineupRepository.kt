package com.telen.easylineup.domain.repository

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TournamentWithLineup
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface LineupRepository {
    fun insertLineup(lineup: Lineup): Single<Long>
    fun insertLineups(lineups: List<Lineup>): Completable
    fun updateLineup(lineup: Lineup): Completable
    fun updateLineupsWithRowCount(lineups: List<Lineup>): Single<Int>
    fun deleteLineup(lineup: Lineup): Completable
    fun deleteLineups(lineups: List<Lineup>): Completable
    fun getAllLineup(): LiveData<List<Lineup>>
    fun getLineups(): Single<List<Lineup>>
    fun getLineupById(lineupId: Long): LiveData<Lineup>
    fun getLineupByHash(hash: String): Single<Lineup>
    fun getLineupByIdSingle(lineupId: Long): Single<Lineup>
    fun getLineupsForTournament(tournamentId: Long, teamID: Long): LiveData<List<Lineup>>
    fun getLineupsForTournamentRx(tournamentId: Long, teamID: Long): Single<List<Lineup>>
    fun getLastLineup(teamID: Long): Maybe<Lineup>
    fun getAllTournamentsWithLineups(filter: String, teamID: Long): Single<List<TournamentWithLineup>>
    fun getAllPlayerPositionsForTournament(tournamentId: Long, teamID: Long): Single<List<PlayerInLineup>>
}