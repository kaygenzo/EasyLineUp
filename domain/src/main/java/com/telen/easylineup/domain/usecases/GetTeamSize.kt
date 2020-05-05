package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.Single

internal class GetTeamSize(val dao: PlayerRepository): UseCase<GetTeamSize.RequestValues, GetTeamSize.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getPlayers(requestValues.team.id)
                .map { TeamSizeData(it.size, teamImage = requestValues.team.image) }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val data: ITileData): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}