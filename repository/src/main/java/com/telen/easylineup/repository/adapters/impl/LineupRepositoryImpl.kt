/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerInLineup
import com.telen.easylineup.domain.model.TournamentWithLineup
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.repository.dao.LineupDao
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toLineup
import com.telen.easylineup.repository.model.toPlayerInLineup
import com.telen.easylineup.repository.model.toTournamentWithLineup
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class LineupRepositoryImpl(private val lineupDao: LineupDao) : LineupRepository {
    init {
        Timber.i("LineupRepositoryImpl.init")
    }

    override fun insertLineup(lineup: Lineup): Single<Long> {
        return lineupDao.insertLineup(RoomLineup().init(lineup))
    }

    override fun insertLineups(lineups: List<Lineup>): Completable {
        return lineupDao.insertLineups(lineups.map { RoomLineup().init(it) })
    }

    override fun updateLineup(lineup: Lineup): Completable {
        return lineupDao.updateLineup(RoomLineup().init(lineup))
    }

    override fun updateLineupsWithRowCount(lineups: List<Lineup>): Single<Int> {
        return lineupDao.updateLineupsWithRowCount(lineups.map { RoomLineup().init(it) })
    }

    override fun deleteLineup(lineup: Lineup): Completable {
        return lineupDao.deleteLineup(RoomLineup().init(lineup))
    }

    override fun deleteLineups(lineups: List<Lineup>): Completable {
        return lineupDao.deleteLineups(lineups.map { RoomLineup().init(it) })
    }

    override fun getAllLineup(): LiveData<List<Lineup>> {
        return lineupDao.getAllLineup().map {
            it.map { it.toLineup() }
        }
    }

    override fun getLineups(): Single<List<Lineup>> {
        return lineupDao.getLineups().map { it.map { it.toLineup() } }
    }

    override fun getLineupById(lineupId: Long): LiveData<Lineup> {
        return lineupDao.getLineupById(lineupId).map {
            it?.toLineup() ?: Lineup()
        }
    }

    override fun getLineupByHash(hash: String): Single<Lineup> {
        return lineupDao.getLineupByHash(hash).map { it.toLineup() }
    }

    override fun getLineupByIdSingle(lineupId: Long): Single<Lineup> {
        return lineupDao.getLineupByIdSingle(lineupId).map { it.toLineup() }
    }

    override fun getLineupsForTournament(tournamentId: Long, teamId: Long): LiveData<List<Lineup>> {
        return lineupDao.getLineupsForTournament(tournamentId, teamId).map {
            it.map { it.toLineup() }
        }
    }

    override fun getLineupsForTournamentRx(tournamentId: Long, teamId: Long): Single<List<Lineup>> {
        return lineupDao.getLineupsForTournamentRx(tournamentId, teamId)
            .map { it.map { it.toLineup() } }
    }

    override fun getLastLineup(teamId: Long): Maybe<Lineup> {
        return lineupDao.getLastLineup(teamId).map { it.toLineup() }
    }

    override fun getAllTournamentsWithLineups(
        filter: String,
        teamId: Long
    ): Single<List<TournamentWithLineup>> {
        return lineupDao.getAllTournamentsWithLineups(filter, teamId)
            .map { it.map { it.toTournamentWithLineup() } }
    }

    override fun getAllPlayerPositionsForTournament(
        tournamentId: Long,
        teamId: Long
    ): Single<List<PlayerInLineup>> {
        return lineupDao.getAllPlayerPositionsForTournament(tournamentId, teamId).map {
            it.map {
                it.toPlayerInLineup()
            }
        }
    }
}
