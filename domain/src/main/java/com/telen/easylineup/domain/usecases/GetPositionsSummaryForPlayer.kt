package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.FieldPosition
import io.reactivex.Single

internal class GetPositionsSummaryForPlayer(val dao: PlayerFieldPositionRepository): UseCase<GetPositionsSummaryForPlayer.RequestValues, GetPositionsSummaryForPlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.playerID?.let { id ->
            dao.getAllPositionsForPlayer(id)
                    .map { list ->
                        val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
                        list.forEach { position ->
                            val fieldPosition = FieldPosition.getFieldPosition(position.position)
                            fieldPosition?.let { element ->
                                chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                            }
                        }
                        ResponseValue(chartData)
                    }
        } ?: Single.error(IllegalArgumentException())
    }

    class ResponseValue(val summary: Map<FieldPosition, Int>): UseCase.ResponseValue
    class RequestValues(val playerID: Long?): UseCase.RequestValues
}