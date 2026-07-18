/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Single

class GetPositionsSummaryForPlayer(
    private val dao: PlayerFieldPositionRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(playerId: Long?): Single<Map<FieldPosition, Int>> {
        return (
            playerId?.let { id ->
                dao.getAllPositionsForPlayer(id)
                    .map { list ->
                        val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
                        list.forEach { position ->
                            val fieldPosition =
                                FieldPosition.getFieldPositionById(position.position)
                            fieldPosition?.let { element ->
                                chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                            }
                        }
                        chartData as Map<FieldPosition, Int>
                    }
            } ?: Single.error(IllegalArgumentException())
            ).subscribeOn(schedulersProvider.io())
    }
}
