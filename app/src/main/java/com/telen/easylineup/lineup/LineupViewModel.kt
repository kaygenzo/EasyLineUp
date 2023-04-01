package com.telen.easylineup.lineup

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.views.LineupTypeface
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

sealed class EventCase

data class LinkDpFlex(
    val initialData: Pair<PlayerWithPosition?, PlayerWithPosition?>,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int,
    @StringRes val title: Int
) : EventCase()

data class ListAvailablePlayers(
    val players: List<PlayerWithPosition>,
    val position: FieldPosition
) : EventCase()

class LineupViewModel : ViewModel(), KoinComponent {
    private val prefsHelper by inject<SharedPreferencesHelper>()
    private val domain: ApplicationInteractor by inject()

    //    private val _designatedPlayerTitle = MutableLiveData<String>()
    private val _helpEvent = MutableLiveData(false)

    // players
    private val _listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()
    private val _players: MediatorLiveData<List<PlayerWithPosition>> by lazy {
        MediatorLiveData<List<PlayerWithPosition>>().apply {
            addSource(getLineupAndPositions()) { this.postValue(it) }
        }
    }
    private val _batters: MutableLiveData<List<BatterState>> = MutableLiveData()

    // lineup
    var lineup: Lineup? = null
        private set
    private val _lineup: MediatorLiveData<Lineup> by lazy {
        MediatorLiveData<Lineup>().apply {
            addSource(getLineup()) { this.postValue(it) }
        }
    }

    var lineupID: Long? = 0
    var editable = false
    private val disposables = CompositeDisposable()

    private fun setLineup(lineup: Lineup) {
        this.lineup = lineup
        _lineup.postValue(this.lineup)
    }

    private fun refreshPlayers(players: List<PlayerWithPosition>) {
        _players.postValue(players)
    }

    fun clearData() {
        disposables.clear()
    }

    fun observeLineupName(): LiveData<String> {
        return Transformations.map(_lineup) { it.name }
    }

    fun observeLineupStrategy(): LiveData<TeamStrategy> {
        return Transformations.map(_lineup) { TeamStrategy.getStrategyById(it.strategy) }
    }

    fun observeLineupMode(): LiveData<Int> {
        return Transformations.map(_lineup) { it.mode }
    }

    fun observeLineup(): LiveData<Lineup> {
        return _lineup
    }

    fun observeLineupTypeface(context: Context): LiveData<LineupTypeface> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val lineupValue = preferences.getString(
            context.getString(R.string.key_lineup_style),
            context.getString(R.string.lineup_style_default_value)
        )
        val lineupTypeface = LineupTypeface.getByValue(lineupValue)
        return MutableLiveData(lineupTypeface)
    }

    fun onDeletePosition(player: Player) {
        val lineupMode = lineup?.mode ?: MODE_DISABLED
        val hitters = lineup?.extraHitters ?: 0
        val disposable = domain
            .playerFieldPositions()
            .deletePlayerPosition(player, _listPlayersWithPosition, lineupMode, hitters)
            .subscribe({
                refreshPlayers(_listPlayersWithPosition)
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }

    fun getTeamStrategy(): Single<TeamStrategy> {
        return domain.lineups().getLineupById(lineupID ?: 0)
            .map { TeamStrategy.getStrategyById(it.strategy) }
            .subscribeOn(Schedulers.io())
    }

    private fun getNotSelectedPlayers(sortBy: FieldPosition?): Single<List<PlayerWithPosition>> {
        return Single.defer {
            lineup?.let {
                domain.lineups().getNotSelectedPlayersFromList(_listPlayersWithPosition, it, sortBy)
            } ?: Single.error(IllegalArgumentException("Lineup is not expected to be null"))
        }
    }

    fun save(): Completable {
        return lineup?.let { domain.lineups().updateLineup(it, _listPlayersWithPosition) }
            ?: Completable.error(IllegalArgumentException("Lineup is not supposed to be null"))
    }

    fun deleteLineup(): Completable {
        return domain.lineups().deleteLineup(lineupID)
    }

    fun getTeamType(): Single<Int> {
        return domain.teams().getTeamType()
    }

    fun onLineupModeChanged(isEnabled: Boolean) {
        lineup?.let { lineup ->
            val disposable =
                domain.lineups().updateLineupMode(isEnabled, lineup, _listPlayersWithPosition)
                    .subscribe({
                        setLineup(lineup)
                        refreshPlayers(_listPlayersWithPosition)
                    }, {
                        Timber.e(it)
                    })
            disposables.add(disposable)
        }
    }

    //TODO use case ?
    fun exportLineupToExternalStorage(
        context: Context,
        views: Map<Int, Bitmap?>
    ): Single<Intent> {
        return Single.defer {
            val tmpPath = context.cacheDir.path
            val timeInMillis = Calendar.getInstance().timeInMillis

            val uris = arrayListOf<Uri>()

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

    fun switchPlayersPosition(p1: PlayerWithPosition, p2: PlayerWithPosition): Completable {
        val position1 = FieldPosition.getFieldPositionById(p1.position) ?: FieldPosition.FIRST_BASE
        val position2 = FieldPosition.getFieldPositionById(p2.position) ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    fun switchPlayersPosition(p1: PlayerWithPosition, position2: FieldPosition): Completable {
        val position1 = FieldPosition.getFieldPositionById(p1.position) ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    private fun switchPlayersPosition(p1: FieldPosition, p2: FieldPosition): Completable {
        return Completable.defer {
            lineup?.let {
                domain.playerFieldPositions()
                    .switchPlayersPosition(p1, p2, _listPlayersWithPosition, it)
                    .doOnComplete { refreshPlayers(_listPlayersWithPosition) }
            } ?: Completable.error(IllegalArgumentException("Lineup is not supposed to be null"))
        }
    }

    fun onPlayerSelected(player: Player, position: FieldPosition) {
        lineup?.let {
            val disposable = domain.playerFieldPositions()
                .savePlayerFieldPosition(player, position, it, _listPlayersWithPosition)
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
                domain.lineups().getDpAndFlexFromPlayersInField(_listPlayersWithPosition)
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

    fun observeBatters(): LiveData<List<BatterState>> {
        return Transformations.switchMap(_players) { players ->
            Transformations.switchMap(_lineup) { lineup ->
                val batterSize = TeamStrategy.getStrategyById(lineup.strategy).batterSize
                val extraHitters = lineup.extraHitters
                val lineupMode = lineup.mode
                val disposable = domain.teams().getTeamType().flatMap {
                    domain.lineups().getBatterStates(
                        players = players,
                        teamType = it,
                        batterSize = batterSize,
                        extraHitterSize = extraHitters,
                        lineupMode = lineupMode,
                        isDebug = BuildConfig.DEBUG,
                        isEditable = editable
                    )
                }.subscribe({
                    _batters.postValue(it)
                }, {
                    Timber.e(it)
                })
                this.disposables.add(disposable)
                _batters
            }
        }
    }

    fun observeDefensePlayers(): LiveData<List<PlayerWithPosition>> {
        return _players
    }

    private fun getLineupAndPositions(): LiveData<List<PlayerWithPosition>> {

        val currentLineupID = lineupID ?: 0

        val getLineup = domain.lineups().observeLineupById(currentLineupID)
        val getPositions = Transformations.switchMap(getLineup) {
            domain.lineups().observeTeamPlayersAndMaybePositionsForLineup(it.id)
        }

        val getPlayerNumberOverlays = Transformations.switchMap(getPositions) { positions ->
            val playerMap = mutableMapOf<Long, PlayerWithPosition>()
            positions.forEach { playerMap[it.playerID] = it }
            Transformations.map(domain.players().observePlayerNumberOverlays(currentLineupID)) {
                it.forEach { overlay -> playerMap[overlay.playerID]?.shirtNumber = overlay.number }
                positions
            }
        }

        return Transformations.map(getPlayerNumberOverlays) {
            _listPlayersWithPosition.clear()
            _listPlayersWithPosition.addAll(it)
            it
        }
    }

    private fun getLineup(): LiveData<Lineup> {
        return Transformations.map(domain.lineups().observeLineupById(lineupID ?: 0)) {
            it.apply {
                this@LineupViewModel.lineup = this
            }
        }
    }

    fun onScreenChanged(position: Int) {
        val show = prefsHelper.isFeatureEnabled(Constants.PREF_FEATURE_SHOW_REORDER_HELP)
        if (position == FRAGMENT_ATTACK_INDEX && editable && show) {
            prefsHelper.disableFeature(Constants.PREF_FEATURE_SHOW_REORDER_HELP)
            _helpEvent.postValue(true)
        } else {
            _helpEvent.postValue(false)
        }
    }

    fun observeHelpEvent(): LiveData<Boolean> {
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
        return domain.lineups().getPlayersInFieldFromList(_listPlayersWithPosition)
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
                domain.lineups().linkDpAndFlex(dp, flex, it, _listPlayersWithPosition)
                    .doOnComplete { refreshPlayers(_listPlayersWithPosition) }
            } ?: Completable.error(IllegalStateException("Lineup cannot be null"))
        }
    }
}

fun <T> Maybe<T>.processError(): Maybe<T> {
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