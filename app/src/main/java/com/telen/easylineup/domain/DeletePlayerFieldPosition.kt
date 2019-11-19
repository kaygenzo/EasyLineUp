package com.telen.easylineup.domain

import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.LineupDao
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerWithPosition

class DeletePlayerFieldPosition(private val dao: LineupDao): UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            try {
                req.players.first { p -> p.playerID == req.player.id && p.position == req.position.position }.let {
                    dao.deletePosition(it.toPlayerFieldPosition()).subscribe({
                        mUseCaseCallback?.onSuccess(ResponseValue())
                    }, {
                        mUseCaseCallback?.onError()
                    })
                }
            }
            catch (e: NoSuchElementException) {
                mUseCaseCallback?.onError()
            }
        }
    }


    class RequestValues(val players: List<PlayerWithPosition>, val player: Player, val position: FieldPosition): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}