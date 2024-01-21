/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.repository.dao.TournamentDao
import com.telen.easylineup.repository.model.RoomTournament
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toTournament
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class TournamentRepositoryImpl(private val tournamentDao: TournamentDao) : TournamentRepository {
    init {
        Timber.i("TournamentRepositoryImpl.init")
    }

    override fun getTournaments(): Single<List<Tournament>> {
        return tournamentDao.getTournaments().map { it.map { it.toTournament() } }
    }

    override fun observeTournaments(): LiveData<List<Tournament>> {
        return tournamentDao.observeTournaments().map { it.map { it.toTournament() } }
    }

    override fun getTournamentByHash(hash: String): Single<Tournament> {
        return tournamentDao.getTournamentByHash(hash).map { it.toTournament() }
    }

    override fun getTournamentByName(name: String): Single<Tournament> {
        return tournamentDao.getTournamentByName(name).map { it.toTournament() }
    }

    override fun insertTournament(tournament: Tournament): Single<Long> {
        return tournamentDao.insertTournament(RoomTournament().init(tournament))
    }

    override fun insertTournaments(tournaments: List<Tournament>): Completable {
        return tournamentDao.insertTournaments(tournaments.map { RoomTournament().init(it) })
    }

    override fun updateTournament(tournament: Tournament): Completable {
        return tournamentDao.updateTournament(RoomTournament().init(tournament))
    }

    override fun updateTournamentsWithRowCount(tournaments: List<Tournament>): Single<Int> {
        return tournamentDao.updateTournamentsWithRowCount(tournaments.map { RoomTournament().init(it) })
    }

    override fun deleteTournament(tournament: Tournament): Completable {
        return tournamentDao.deleteTournament(RoomTournament().init(tournament))
    }

    override fun deleteTournaments(tournaments: List<Tournament>): Completable {
        return tournamentDao.deleteTournaments(tournaments.map { RoomTournament().init(it) })
    }
}
