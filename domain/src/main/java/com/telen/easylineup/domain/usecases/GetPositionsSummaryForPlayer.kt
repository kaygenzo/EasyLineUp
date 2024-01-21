/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class GetPositionsSummaryForPlayer(val dao: PlayerFieldPositionRepository) :
    UseCase<GetPositionsSummaryForPlayer.RequestValues,
GetPositionsSummaryForPlayer.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.playerId?.let { id ->
            dao.getAllPositionsForPlayer(id)
                .map { list ->
                    val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
                    list.forEach { position ->
                        val fieldPosition = FieldPosition.getFieldPositionById(position.position)
                        fieldPosition?.let { element ->
                            chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                        }
                    }
                    ResponseValue(chartData)
                }
        } ?: Single.error(IllegalArgumentException())
    }

    /**
     * @property summary
     */
    class ResponseValue(val summary: Map<FieldPosition, Int>) : UseCase.ResponseValue

    /**
     * @property playerId
     */
    class RequestValues(val playerId: Long?) : UseCase.RequestValues
}
