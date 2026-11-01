/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.tiles.LastLineupData
import com.telen.easylineup.domain.model.tiles.MostUsedPlayerData
import com.telen.easylineup.domain.model.tiles.TeamSizeData
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.usecases.GetDashboardTiles
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.ObserveTeams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Everything [DashboardScreen] needs to render, owned end to end by [DashboardViewModel] -
 * the Route only collects [DashboardViewModel.uiState] and forwards user actions back as
 * method calls, it holds no state of its own.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val teamName: String = "",
    val teamImageUri: Uri? = null,
    val teamSize: Int? = null,
    val teamType: TeamType = TeamType.UNKNOWN,
    val mostUsedPlayer: MostUsedPlayerUi? = null,
    val lastLineup: LastLineupUi? = null,
    val shirtNumberQuery: String = "",
    val shirtNumberHasSearched: Boolean = false,
    val shirtNumberResult: String? = null,
    val shirtNumberHistory: List<ShirtNumberEntry> = emptyList(),
    val showShirtNumberHistoryDialog: Boolean = false
)

class DashboardViewModel(
    private val observeTeams: ObserveTeams,
    private val getDashboardTilesUseCase: GetDashboardTiles,
    private val getShirtNumberHistoryUseCase: GetShirtNumberHistory
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTeams().catch { Timber.e(it) }
                .flatMapLatest { getDashboardTilesFlow() }
                .collect { tiles -> applyTiles(tiles) }
        }
    }

    private fun getDashboardTilesFlow(): Flow<List<DashboardTile>> = flow {
        getDashboardTilesUseCase()
            .onSuccess { emit(it) }
            .onFailure { Timber.e(it) }
    }

    private fun applyTiles(tiles: List<DashboardTile>) {
        // Each emission is a fresh team's tile set (possibly missing a type entirely when
        // that team has no games/lineups yet) - a brand new DashboardUiState recomputes every
        // field from scratch (search state included) instead of only updating the types
        // present, or a team switch would leave the previous team's state stuck on screen.
        val teamSizeData = tiles.firstOrNull { it.type == TileType.TEAM_SIZE.type }
            ?.data as? TeamSizeData
        val mostUsedData = tiles.firstOrNull { it.type == TileType.MOST_USED_PLAYER.type }
            ?.data as? MostUsedPlayerData
        val lastLineupData = tiles.firstOrNull { it.type == TileType.LAST_LINEUP.type }
            ?.data as? LastLineupData

        _uiState.update {
            DashboardUiState(
                isLoading = false,
                teamName = teamSizeData?.teamName ?: "",
                teamImageUri = teamSizeData?.teamImage?.let(Uri::parse),
                teamSize = teamSizeData?.size,
                teamType = teamSizeData?.let { data -> TeamType.getTypeById(data.teamType) }
                    ?: TeamType.UNKNOWN,
                mostUsedPlayer = mostUsedData?.let { data ->
                    MostUsedPlayerUi(data.name.trim(), data.shirtNumber, data.matchPlayed)
                },
                lastLineup = lastLineupData?.let { data ->
                    LastLineupUi(data.lineupId, data.name, data.eventTimeInMillis, data.tournamentName)
                }
            )
        }
    }

    fun onShirtNumberQueryChanged(query: String) {
        _uiState.update { it.copy(shirtNumberQuery = query.filter(Char::isDigit)) }
    }

    /** Returns whether a search was actually started, for the Route to gate analytics. */
    fun onShirtNumberSearch(): Boolean {
        val number = _uiState.value.shirtNumberQuery.toIntOrNull() ?: return false
        viewModelScope.launch {
            getShirtNumberHistoryUseCase(number)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            shirtNumberHasSearched = true,
                            shirtNumberResult = history.firstOrNull()?.playerName,
                            shirtNumberHistory = history
                        )
                    }
                }
                .onFailure { Timber.e(it) }
        }
        return true
    }

    /** Returns whether the history sheet was actually opened, for the Route to gate analytics. */
    fun onShirtNumberHistoryClick(): Boolean {
        val hasHistory = _uiState.value.shirtNumberHistory.isNotEmpty()
        if (hasHistory) {
            _uiState.update { it.copy(showShirtNumberHistoryDialog = true) }
        }
        return hasHistory
    }

    fun onShirtNumberHistoryDismiss() {
        _uiState.update { it.copy(showShirtNumberHistoryDialog = false) }
    }
}
