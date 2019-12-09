package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class CreateLineup(private val tournamentDao: TournamentDao, private val lineupsDao: LineupDao): UseCase<CreateLineup.RequestValues, CreateLineup.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {

        return Single.just(requestValues.tournament)
                .flatMap {
                    if(it.id == 0L)
                        tournamentDao.insertTournament(it)
                    else
                        tournamentDao.updateTournament(it).andThen(Single.just(it.id))
                }
                .flatMap {
                    val newLineup = Lineup(name = requestValues.lineupTitle, teamId = requestValues.teamID, tournamentId = it)
                    lineupsDao.insertLineup(newLineup)
                }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val lineupID: Long): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val tournament: Tournament, val lineupTitle: String): UseCase.RequestValues
}