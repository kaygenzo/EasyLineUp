/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.repository.dao.TournamentDao
import com.telen.easylineup.repository.model.toDomain
import com.telen.easylineup.repository.model.toRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class TournamentRepositoryImpl(
    private val tournamentDao: TournamentDao
) : TournamentRepository {
    init {
        Timber.i("TournamentRepositoryImpl.init")
    }

    override suspend fun getTournaments(): List<Tournament> {
        return tournamentDao.getTournaments().map { it.toDomain() }
    }

    override fun observeTournaments(): Flow<List<Tournament>> {
        return tournamentDao.observeTournaments().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTournamentByHash(hash: String): Tournament {
        return tournamentDao.getTournamentByHash(hash).toDomain()
    }

    override suspend fun getTournamentByName(name: String): Tournament {
        return tournamentDao.getTournamentByName(name).toDomain()
    }

    override suspend fun insertTournament(tournament: Tournament): Long {
        return tournamentDao.insertTournament(tournament.toRoom())
    }

    override suspend fun insertTournaments(tournaments: List<Tournament>) {
        tournamentDao.insertTournaments(tournaments.map { it.toRoom() })
    }

    override suspend fun updateTournament(tournament: Tournament) {
        tournamentDao.updateTournament(tournament.toRoom())
    }

    override suspend fun updateTournamentsWithRowCount(tournaments: List<Tournament>): Int {
        return tournamentDao.updateTournamentsWithRowCount(tournaments.map { it.toRoom() })
    }

    override suspend fun deleteTournament(tournament: Tournament) {
        tournamentDao.deleteTournament(tournament.toRoom())
    }

    override suspend fun deleteTournaments(tournaments: List<Tournament>) {
        tournamentDao.deleteTournaments(tournaments.map { it.toRoom() })
    }
}
