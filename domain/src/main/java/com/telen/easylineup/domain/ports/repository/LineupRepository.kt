/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports.repository

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TournamentWithLineup
import kotlinx.coroutines.flow.Flow

interface LineupRepository {
    suspend fun insertLineup(lineup: Lineup): Long
    suspend fun insertLineups(lineups: List<Lineup>)
    suspend fun updateLineup(lineup: Lineup)
    suspend fun updateLineupsWithRowCount(lineups: List<Lineup>): Int
    suspend fun deleteLineup(lineup: Lineup)
    suspend fun deleteLineups(lineups: List<Lineup>)
    suspend fun getLineups(): List<Lineup>
    fun observeLineupById(lineupId: Long): Flow<Lineup>
    suspend fun getLineupByHash(hash: String): Lineup?
    suspend fun getLineupById(lineupId: Long): Lineup
    suspend fun getLineupsForTournament(tournamentId: Long, teamId: Long): List<Lineup>
    suspend fun getLastLineup(teamId: Long): Lineup?
    suspend fun getAllTournamentsWithLineups(
        filter: String,
        teamId: Long
    ): List<TournamentWithLineup>

    suspend fun getAllPlayerPositionsForTournament(
        tournamentId: Long,
        teamId: Long
    ): List<PlayerInLineup>
}
