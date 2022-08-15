package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.Tournament
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TournamentRepository {
    fun getTournaments(): Single<List<Tournament>>
    fun getTournamentByHash(hash: String): Single<Tournament>
    fun insertTournament(tournament: Tournament): Single<Long>
    fun insertTournaments(tournaments: List<Tournament>): Completable
    fun updateTournament(tournament: Tournament): Completable
    fun updateTournamentsWithRowCount(tournaments: List<Tournament>): Single<Int>
    fun deleteTournament(tournament: Tournament): Completable
    fun deleteTournaments(tournaments: List<Tournament>): Completable
}