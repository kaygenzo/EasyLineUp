package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.Single

internal class GetMostUsedPlayer(val dao: PlayerFieldPositionRepository, val playerDao: PlayerRepository): UseCase<GetMostUsedPlayer.RequestValues, GetMostUsedPlayer.ResponseValue>() {

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