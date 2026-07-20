/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.withContext

class GetPositionsSummaryForPlayer(
    private val dao: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(playerId: Long?): Result<Map<FieldPosition, Int>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val id = playerId ?: throw IllegalArgumentException()
            val list = dao.getAllPositionsForPlayer(id)
            val chartData: MutableMap<FieldPosition, Int> = mutableMapOf()
            list.forEach { position ->
                val fieldPosition = FieldPosition.getFieldPositionById(position.position)
                fieldPosition?.let { element ->
                    chartData[element] = chartData[element]?.let { it + 1 } ?: 1
                }
            }
            chartData as Map<FieldPosition, Int>
        }
    }
}
