package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class DeletePlayer(private val dao: PlayerDao): UseCase<DeletePlayer.RequestValues, DeletePlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.deletePlayer(requestValues.player).andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val player: Player): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}