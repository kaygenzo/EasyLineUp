package com.telen.easylineup.domain

import android.annotation.SuppressLint
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.MostUsedPlayerData
import com.telen.easylineup.dashboard.models.TeamSizeData
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Maybe
import io.reactivex.Single

class GetMostUsedPlayer(val dao: PlayerFieldPositionsDao, val playerDao: PlayerDao): UseCase<GetMostUsedPlayer.RequestValues, GetMostUsedPlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getMostUsedPlayers(requestValues.team.id)
                .flatMap { list ->
                    try {
                        val mostUsed = list.first()
                        playerDao.getPlayerByIdAsSingle(mostUsed.playerID)
                                .map { player -> MostUsedPlayerData(player.image, player.name, player.shirtNumber, mostUsed.size) }
                                .map { ResponseValue(it) }
                    } catch (e: NoSuchElementException) {
                        Single.just(ResponseValue(null))
                    }
                }
    }

    class ResponseValue(val data: ITileData?): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}