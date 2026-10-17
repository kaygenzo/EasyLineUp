/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.dashboard

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.ObserveTeams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class DashboardViewModel(
    private val observeTeams: ObserveTeams,
    private val getDashboardTilesUseCase: GetDashboardTiles,
    private val getShirtNumberHistoryUseCase: GetShirtNumberHistory
) : ViewModel() {
    fun registerTilesFlow(): Flow<List<DashboardTile>> = observeTeams().catch { Timber.e(it) }
        .flatMapLatest {
            getDashboardTilesFlow()
        }

    private fun getDashboardTilesFlow(): Flow<List<DashboardTile>> = flow {
        getDashboardTilesUseCase()
            .onSuccess { emit(it) }
            .onFailure { Timber.e(it) }
    }

    suspend fun getShirtNumberHistory(number: Int) = getShirtNumberHistoryUseCase(number)
}
