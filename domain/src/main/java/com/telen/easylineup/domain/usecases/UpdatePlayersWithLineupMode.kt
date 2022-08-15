package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class UpdatePlayersWithLineupMode(private val lineupDao: PlayerFieldPositionRepository): UseCase<UpdatePlayersWithLineupMode.RequestValues, UpdatePlayersWithLineupMode.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val playerTask: Completable = when(requestValues.isDesignatedPlayerEnabled) {
            true -> {
                when(requestValues.teamType) {
                    TeamType.SOFTBALL.id -> {
                        Completable.complete()
                    }
                    TeamType.BASEBALL.id -> {
                        // find the pitcher if exists and set him at position 10 in lineup
                        requestValues.players.firstOrNull { it.position == FieldPosition.PITCHER.id }?.let {
                            val playerFieldPosition = it.toPlayerFieldPosition()
                            //here we use directly the standard strategy because we only have one strategy in baseball
                            playerFieldPosition.order = requestValues.strategy.getDesignatedPlayerOrder(requestValues.extraHittersSize)
                            playerFieldPosition.flags = PlayerFieldPosition.FLAG_FLEX
                            lineupDao.updatePlayerFieldPosition(playerFieldPosition)
                        } ?: Completable.complete()
                    }
                    else -> {
                        Completable.error(IllegalArgumentException())
                    }
                }
            }
            false -> {
                requestValues.players.filter {
                    it.position == FieldPosition.DP_DH.id || (it.flags and PlayerFieldPosition.FLAG_FLEX > 0)
                }.let { list ->
                    Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                        lineupDao.deletePosition(playerPosition.toPlayerFieldPosition())
                    }
                }
            }
        }
        return playerTask.andThen(Single.just(ResponseValue()))
    }

    class RequestValues(val players: List<PlayerWithPosition>, val isDesignatedPlayerEnabled: Boolean, val teamType: Int, val strategy: TeamStrategy, val extraHittersSize: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}