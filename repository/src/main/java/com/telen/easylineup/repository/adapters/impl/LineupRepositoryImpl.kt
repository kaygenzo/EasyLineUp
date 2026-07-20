/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TournamentWithLineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.repository.dao.LineupDao
import com.telen.easylineup.repository.model.toDomain
import com.telen.easylineup.repository.model.toRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class LineupRepositoryImpl(private val lineupDao: LineupDao) : LineupRepository {
    init {
        Timber.i("LineupRepositoryImpl.init")
    }

    override suspend fun insertLineup(lineup: Lineup): Long {
        return lineupDao.insertLineup(lineup.toRoom())
    }

    override suspend fun insertLineups(lineups: List<Lineup>) {
        lineupDao.insertLineups(lineups.map { it.toRoom() })
    }

    override suspend fun updateLineup(lineup: Lineup) {
        lineupDao.updateLineup(lineup.toRoom())
    }

    override suspend fun updateLineupsWithRowCount(lineups: List<Lineup>): Int {
        return lineupDao.updateLineupsWithRowCount(lineups.map { it.toRoom() })
    }

    override suspend fun deleteLineup(lineup: Lineup) {
        lineupDao.deleteLineup(lineup.toRoom())
    }

    override suspend fun deleteLineups(lineups: List<Lineup>) {
        lineupDao.deleteLineups(lineups.map { it.toRoom() })
    }

    override suspend fun getLineups(): List<Lineup> {
        return lineupDao.getLineups().map { it.toDomain() }
    }

    override fun getLineupById(lineupId: Long): Flow<Lineup> {
        return lineupDao.getLineupById(lineupId).map {
            it.firstOrNull()?.toDomain() ?: Lineup()
        }
    }

    override suspend fun getLineupByHash(hash: String): Lineup {
        return lineupDao.getLineupByHash(hash).toDomain()
    }

    override suspend fun getLineupByIdSingle(lineupId: Long): Lineup {
        return lineupDao.getLineupByIdSingle(lineupId).toDomain()
    }

    override suspend fun getLineupsForTournamentRx(tournamentId: Long, teamId: Long): List<Lineup> {
        return lineupDao.getLineupsForTournamentRx(tournamentId, teamId)
            .map { it.toDomain() }
    }

    override suspend fun getLastLineup(teamId: Long): Lineup? {
        return lineupDao.getLastLineup(teamId)?.toDomain()
    }

    override suspend fun getAllTournamentsWithLineups(
        filter: String,
        teamId: Long
    ): List<TournamentWithLineup> {
        return lineupDao.getAllTournamentsWithLineups(filter, teamId)
            .map { it.toDomain() }
    }

    override suspend fun getAllPlayerPositionsForTournament(
        tournamentId: Long,
        teamId: Long
    ): List<PlayerInLineup> {
        return lineupDao.getAllPlayerPositionsForTournament(tournamentId, teamId).map {
            it.toDomain()
        }
    }
}
