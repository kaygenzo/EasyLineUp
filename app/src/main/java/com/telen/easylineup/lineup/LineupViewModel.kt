/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.telen.easylineup.lineup

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.DeleteLineup
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetBattersState
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetListAvailablePlayersForSelection
import com.telen.easylineup.domain.usecases.GetLineupById
import com.telen.easylineup.domain.usecases.GetOnlyPlayersInField
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObserveLineupById
import com.telen.easylineup.domain.usecases.ObservePlayerNumberOverlays
import com.telen.easylineup.domain.usecases.ObserveTeamPlayersAndMaybePositionsForLineup
import com.telen.easylineup.domain.usecases.SaveBattingOrderAndPositions
import com.telen.easylineup.domain.usecases.SaveDpAndFlex
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import com.telen.easylineup.domain.usecases.UpdatePlayersWithBatters
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.utils.asSafeFlow
import com.telen.easylineup.views.LineupTypeface
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

sealed class EventCase

/**
 * @property initialData
 * @property dpLocked
 * @property flexLocked
 * @property teamType
 * @property title
 */
data class LinkDpFlex(
    val initialData: Pair<PlayerWithPosition?, PlayerWithPosition?>,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int,
    @StringRes val title: Int
) : EventCase()

/**
 * @property players
 * @property position
 */
data class ListAvailablePlayers(
    val players: List<PlayerWithPosition>,
    val position: FieldPosition
) : EventCase()

class LineupViewModel : ViewModel(), KoinComponent {
    private val prefsHelper by inject<SharedPreferencesHelper>()
    private val context: Context by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val observePlayerNumberOverlays: ObservePlayerNumberOverlays by inject()
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition by inject()
    private val switchPlayersPositionUseCase: SwitchPlayersPosition by inject()
    private val assignPlayerFieldPositionUseCase: AssignPlayerFieldPosition by inject()
    private val getLineupByIdUseCase: GetLineupById by inject()
    private val getListAvailablePlayersForSelectionUseCase: GetListAvailablePlayersForSelection
    by inject()
    private val saveBattingOrderAndPositionsUseCase: SaveBattingOrderAndPositions by inject()
    private val deleteLineupUseCase: DeleteLineup by inject()
    private val setLineupModeUseCase: SetLineupMode by inject()
    private val getDpAndFlexFromPlayersInFieldUseCase: GetDpAndFlexFromPlayersInField by inject()
    private val getBattersStateUseCase: GetBattersState by inject()
    private val observeTeamPlayersAndMaybePositionsForLineupUseCase:
    ObserveTeamPlayersAndMaybePositionsForLineup by inject()
    private val observeLineupByIdUseCase: ObserveLineupById by inject()
    private val getOnlyPlayersInFieldUseCase: GetOnlyPlayersInField by inject()
    private val saveDpAndFlexUseCase: SaveDpAndFlex by inject()
    private val updatePlayersWithBattersUseCase: UpdatePlayersWithBatters by inject()

    // private val _designatedPlayerTitle = MutableStateFlow<String>()
    private val _helpEvent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // players
    private val _listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    // Direct pushes from refreshPlayers(), merged with the DB-observed source below -
    // emulates the former MediatorLiveData.addSource() wiring (source + ad-hoc direct writes).
    private val _directPlayersPush: MutableSharedFlow<List<PlayerWithPosition>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val _players: Flow<List<PlayerWithPosition>> by lazy {
        merge(getLineupAndPositions(), _directPlayersPush)
    }
    private val _batters: MutableSharedFlow<List<BatterState>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    // lineup
    var lineup: Lineup? = null
        private set

    // Direct pushes from setLineup(), merged with the DB-observed source below - same pattern
    // as _directPlayersPush/_players above.
    private val _directLineupPush: MutableSharedFlow<Lineup> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val _lineup: Flow<Lineup> by lazy {
        merge(getLineup(), _directLineupPush)
    }
    var lineupId: Long? = 0
    var editable = false
    private val disposables = CompositeDisposable()

    private fun setLineup(lineup: Lineup) {
        this.lineup = lineup
        _directLineupPush.tryEmit(lineup)
    }

    private fun refreshPlayers(players: List<PlayerWithPosition>) {
        _directPlayersPush.tryEmit(players)
    }

    fun clearData() {
        disposables.clear()
    }

    fun observeLineupName(): Flow<String> {
        return _lineup.map { it.name }
    }

    fun observeLineupStrategy(): Flow<TeamStrategy> {
        return _lineup.map { TeamStrategy.getStrategyById(it.strategy) }
    }

    fun observeLineupMode(): Flow<Int> {
        return _lineup.map { it.mode }
    }

    fun observeLineup(): Flow<Lineup> {
        return _lineup
    }

    fun observeLineupTypeface(context: Context): Flow<LineupTypeface> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lineupValue = preferences.getString(
            context.getString(R.string.key_lineup_style),
            context.getString(R.string.lineup_style_default_value)
        )
        val lineupTypeface = LineupTypeface.getByValue(lineupValue)
        return flowOf(lineupTypeface)
    }

    fun onDeletePosition(player: Player) {
        val lineupMode = lineup?.mode ?: MODE_DISABLED
        val hitters = lineup?.extraHitters ?: 0
        val disposable = deletePlayerFieldPositionUseCase(
            _listPlayersWithPosition,
            player,
            lineupMode,
            hitters
        )
            .subscribe({
                refreshPlayers(_listPlayersWithPosition)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }

    fun getTeamStrategy(): Single<TeamStrategy> {
        return getLineupByIdUseCase(lineupId ?: 0)
            .map { TeamStrategy.getStrategyById(it.strategy) }
            .subscribeOn(Schedulers.io())
    }

    private fun getNotSelectedPlayers(sortBy: FieldPosition?): Single<List<PlayerWithPosition>> {
        return Single.defer {
            lineup?.let {
                getListAvailablePlayersForSelectionUseCase(
                    _listPlayersWithPosition,
                    sortBy,
                    it
                )
            } ?: Single.error(IllegalArgumentException("Lineup is not expected to be null"))
        }
    }

    fun save(): Completable {
        return lineup?.let {
            saveBattingOrderAndPositionsUseCase(it, _listPlayersWithPosition)
        } ?: Completable.error(IllegalArgumentException("Lineup is not supposed to be null"))
    }

    fun deleteLineup(): Completable {
        return deleteLineupUseCase(lineupId)
    }

    fun getTeamType(): Single<Int> {
        return getTeamUseCase().map { it.type }
    }

    fun onLineupModeChanged(isEnabled: Boolean) {
        lineup?.let { lineup ->
            val disposable = setLineupModeUseCase(isEnabled, lineup, _listPlayersWithPosition)
                .subscribe({
                    setLineup(lineup)
                    refreshPlayers(_listPlayersWithPosition)
                }, {
                    Timber.e(it)
                })
            disposables.add(disposable)
        }
    }

    // TODO use case ?
    fun exportLineupToExternalStorage(
        context: Context,
        views: Map<Int, Bitmap?>
    ): Single<Intent> {
        return Single.defer {
            val tmpPath = context.cacheDir.path
            val timeInMillis = Calendar.getInstance().timeInMillis

            val uris: ArrayList<Uri> = arrayListOf()

            views.forEach { entry ->
                val pathBuilder = StringBuilder(tmpPath)
                    .append("/")
                    .append(timeInMillis)
                    .append("_")
                when (entry.key) {
                    FRAGMENT_DEFENSE_INDEX -> pathBuilder.append("defense.png")
                    FRAGMENT_ATTACK_INDEX -> pathBuilder.append("attack.png")
                    else -> pathBuilder.append("all.png")
                }
                val filePath = pathBuilder.toString()
                val authority = context.packageName + ".fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, File(filePath))
                try {
                    FileOutputStream(filePath).use {
                        entry.value?.run {
                            compress(Bitmap.CompressFormat.PNG, 100, it)
                                .takeIf { true }?.let { uris.add(uri) }
                        }
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }

            val title = lineup?.name.toString()
            val message = context.getString(R.string.share_lineup_subject, title)
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).run {
                putExtra(Intent.EXTRA_SUBJECT, message)
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            }
            Single.just(intent)
        }
    }

    fun switchPlayersPosition(
        player1: PlayerWithPosition,
        player2: PlayerWithPosition
    ): Completable {
        val position1 = FieldPosition.getFieldPositionById(player1.position)
            ?: FieldPosition.FIRST_BASE
        val position2 = FieldPosition.getFieldPositionById(player2.position)
            ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    fun switchPlayersPosition(player1: PlayerWithPosition, position2: FieldPosition): Completable {
        val position1 = FieldPosition.getFieldPositionById(player1.position)
            ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    private fun switchPlayersPosition(
        position1: FieldPosition,
        position2: FieldPosition
    ): Completable {
        return Completable.defer {
            lineup?.let {
                switchPlayersPositionUseCase(
                    _listPlayersWithPosition,
                    position1,
                    position2,
                    it
                )
                    .doOnComplete { refreshPlayers(_listPlayersWithPosition) }
            } ?: Completable.error(IllegalArgumentException("Lineup is not supposed to be null"))
        }
    }

    fun onPlayerSelected(player: Player, position: FieldPosition) {
        lineup?.let {
            val disposable = assignPlayerFieldPositionUseCase(
                player,
                position,
                it,
                _listPlayersWithPosition
            )
                .subscribe({
                    refreshPlayers(_listPlayersWithPosition)
                }, {
                    Timber.e(it)
                })
            disposables.add(disposable)
        }
    }

    fun onPlayerClicked(position: FieldPosition): Maybe<EventCase> {
        return Maybe.defer {
            if (position != FieldPosition.DP_DH) {
                getNotSelectedPlayers(sortBy = position).flatMapMaybe {
                    Maybe.just(ListAvailablePlayers(it, position))
                }
            } else {
                getDpAndFlexFromPlayersInFieldUseCase(_listPlayersWithPosition)
                    .flatMapMaybe {
                        val title = if (it.teamType == TeamType.SOFTBALL.id) {
                            R.string.link_dp_and_flex_dialog_title
                        } else {
                            R.string.link_dh_and_pitcher_dialog_title
                        }
                        Maybe.just(
                            LinkDpFlex(
                                Pair(it.dp, it.flex),
                                it.dpLocked,
                                it.flexLocked,
                                it.teamType,
                                title
                            )
                        )
                    }
            }
        }.processError()
    }

    fun observeBatters(): Flow<List<BatterState>> {
        return _players.flatMapLatest { players ->
            _lineup.flatMapLatest { lineup ->
                val batterSize = TeamStrategy.getStrategyById(lineup.strategy).batterSize
                val extraHitters = lineup.extraHitters
                val disposable = getTeamUseCase()
                    .map { it.type }
                    .flatMap {
                        getBattersStateUseCase(
                            context = context,
                            players = players,
                            teamType = it,
                            batterSize = batterSize,
                            extraHitterSize = extraHitters,
                            isDebug = BuildConfig.DEBUG,
                            isEditable = editable
                        )
                    }.subscribe({
                    _batters.tryEmit(it)
                }, {
                    Timber.e(it)
                })
                this.disposables.add(disposable)
                _batters
            }
        }
    }

    fun observeDefensePlayers(): Flow<List<PlayerWithPosition>> {
        return _players
    }

    private fun getLineupAndPositions(): Flow<List<PlayerWithPosition>> {
        return getLineup()
            .flatMapLatest {
                observeTeamPlayersAndMaybePositionsForLineupUseCase(it.id).asSafeFlow()
            }
            .flatMapLatest { positions ->
                _listPlayersWithPosition.clear()
                _listPlayersWithPosition.addAll(positions)
                val playerMap = mutableMapOf(
                    *positions.map { Pair(it.playerId, it) }.toTypedArray()
                )
                val currentLineupId = lineupId ?: 0
                observePlayerNumberOverlays(currentLineupId).asSafeFlow()
                    .map {
                        it.forEach { overlay ->
                            playerMap[overlay.playerId]?.shirtNumber = overlay.number
                        }
                        positions
                    }
            }
    }

    private fun getLineup(): Flow<Lineup> {
        return observeLineupByIdUseCase(lineupId ?: 0).asSafeFlow().map {
            it.apply {
                this@LineupViewModel.lineup = this
            }
        }
    }

    fun onScreenChanged(position: Int) {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_REORDER_HELP)
        if (position == FRAGMENT_ATTACK_INDEX && editable && show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_REORDER_HELP)
            _helpEvent.value = true
        } else {
            _helpEvent.value = false
        }
    }

    fun observeHelpEvent(): Flow<Boolean> {
        return _helpEvent
    }

    fun getPlayerSelectionForDp(): Single<List<PlayerWithPosition>> {
        return getNotSelectedPlayers(null)
            .onErrorResumeNext {
                Timber.e(it)
                Single.just(listOf())
            }
    }

    fun getPlayerSelectionForFlex(): Single<List<PlayerWithPosition>> {
        return getOnlyPlayersInFieldUseCase(_listPlayersWithPosition)
            .onErrorResumeNext {
                if (it is NoSuchElementException) {
                    Timber.e(it.message.toString())
                } else {
                    Timber.e(it)
                }
                Single.just(listOf())
            }
    }

    fun linkDpAndFlex(dp: Player?, flex: Player?): Completable {
        return Completable.defer {
            lineup?.let {
                saveDpAndFlexUseCase(it, dp, flex, _listPlayersWithPosition)
                    .doOnComplete { refreshPlayers(_listPlayersWithPosition) }
            } ?: Completable.error(IllegalStateException("Lineup cannot be null"))
        }
    }

    fun onBattersChanged(batters: List<BatterState>): Completable {
        return updatePlayersWithBattersUseCase(_listPlayersWithPosition, batters)
    }
}

fun <T: Any> Maybe<T>.processError(): Maybe<T> {
    return this.onErrorResumeNext {
        when (it) {
            is NeedAssignPitcherFirstException,
            is NoSuchElementException -> {
                Timber.e(it.message.toString())
                Maybe.empty()
            }

            else -> Maybe.error(it)
        }
    }
}
