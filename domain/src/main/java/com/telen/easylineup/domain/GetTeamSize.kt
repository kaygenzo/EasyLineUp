package com.telen.easylineup.domain

import com.telen.easylineup.repository.tiles.ITileData
import com.telen.easylineup.repository.tiles.TeamSizeData
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single

class GetTeamSize(val dao: PlayerDao): UseCase<GetTeamSize.RequestValues, GetTeamSize.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getPlayers(requestValues.team.id)
                .map { TeamSizeData(it.size, teamImage = requestValues.team.image) }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val data: ITileData): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}