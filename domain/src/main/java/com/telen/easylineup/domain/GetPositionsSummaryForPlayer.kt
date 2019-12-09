package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.FieldPosition
import io.reactivex.Single

class GetPositionsSummaryForPlayer(val dao: PlayerFieldPositionsDao): UseCase<GetPositionsSummaryForPlayer.RequestValues, GetPositionsSummaryForPlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        requestValues.playerID?.let { id ->
            return dao.getAllPositionsForPlayer(id)
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
        } ?: throw IllegalArgumentException()
    }

    class ResponseValue(val summary: Map<FieldPosition, Int>): UseCase.ResponseValue
    class RequestValues(val playerID: Long?): UseCase.RequestValues
}