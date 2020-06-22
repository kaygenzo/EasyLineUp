package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.Single

internal class GetLastLineup(private val lineupDao: LineupRepository, private val playerFieldPositionDao: PlayerFieldPositionRepository): UseCase<GetLastLineup.RequestValues, GetLastLineup.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return lineupDao.getLastLineup(requestValues.team.id)
                .flatMap { lineup ->
                    playerFieldPositionDao.getAllPlayersWithPositionsForLineupRx(lineup.id)
                            .map { LastLineupData(lineup.id, lineup.name, it) }
                }
                .map { ResponseValue(it) }
                .onErrorResumeNext {
                    Single.just(ResponseValue(null))
                }
    }

    class ResponseValue(val data: ITileData?): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}