package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Single

class DeletePlayerFieldPosition(private val dao: PlayerFieldPositionsDao): UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return try {
            requestValues.players.first { p -> p.playerID == requestValues.player.id && p.position == requestValues.position.position }.let {
                dao.deletePosition(it.toPlayerFieldPosition()).andThen(Single.just(ResponseValue()))
            }
        }
        catch (e: NoSuchElementException) {
            Single.error(e)
        }
    }


    class RequestValues(val players: List<PlayerWithPosition>, val player: Player, val position: FieldPosition): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}