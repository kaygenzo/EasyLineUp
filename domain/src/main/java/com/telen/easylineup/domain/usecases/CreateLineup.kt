package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.Single
import java.lang.Exception

internal class CreateLineup(private val tournamentDao: TournamentRepository, private val lineupsDao: LineupRepository): UseCase<CreateLineup.RequestValues, CreateLineup.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.tournament)
                .flatMap {
                    when {
                        "" == requestValues.lineupTitle.trim() -> {
                            Single.error(LineupNameEmptyException())
                        }
                        "" == it.name.trim() -> {
                            Single.error(TournamentNameEmptyException())
                        }
                        it.id == 0L -> tournamentDao.insertTournament(it)
                        else -> tournamentDao.updateTournament(it).andThen(Single.just(it.id))
                    }
                }
                .flatMap {
                    val roster = if(requestValues.roster.none { !it.status }) null else rosterToString(requestValues.roster)
                    val newLineup = Lineup(name = requestValues.lineupTitle, teamId = requestValues.teamID, tournamentId = it,
                            eventTimeInMillis = requestValues.lineupEventTime, roster = roster)
                    lineupsDao.insertLineup(newLineup)
                }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val lineupID: Long): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val tournament: Tournament, val lineupTitle: String, val lineupEventTime: Long, val roster: List<RosterPlayerStatus>): UseCase.RequestValues

    private fun rosterToString(list: List<RosterPlayerStatus>): String {
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