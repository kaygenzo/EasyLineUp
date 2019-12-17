package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single
import java.lang.StringBuilder

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
                    val roaster = if(requestValues.roaster.none { !it.status }) null else roasterToString(requestValues.roaster)
                    val newLineup = Lineup(name = requestValues.lineupTitle, teamId = requestValues.teamID, tournamentId = it, roaster = roaster)
                    lineupsDao.insertLineup(newLineup)
                }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val lineupID: Long): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val tournament: Tournament, val lineupTitle: String, val roaster: List<RoasterPlayerStatus>): UseCase.RequestValues

    private fun roasterToString(list: List<RoasterPlayerStatus>): String {
        val builder = StringBuilder()
        list.forEach {
            if(it.status) {
                if(builder.isNotEmpty())
                    builder.append(";")
                builder.append(it.player.id)
            }
        }
        return builder.toString()
    }

}