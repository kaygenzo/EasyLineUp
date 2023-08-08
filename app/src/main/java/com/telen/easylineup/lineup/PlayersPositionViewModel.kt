package com.telen.easylineup.lineup

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.utils.DialogFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

data class InsufficientPermissions(val permissionsNeeded: Array<String>): Exception() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InsufficientPermissions

        if (!permissionsNeeded.contentEquals(other.permissionsNeeded)) return false

        return true
    }

    override fun hashCode(): Int {
        return permissionsNeeded.contentHashCode()
    }

}

sealed class EventCase
object SavePlayerPositionSuccess: EventCase()
object DeletePlayerPositionSuccess: EventCase()
object SaveBattingOrderSuccess: EventCase()
object DeleteLineupSuccess: EventCase()
object UpdatePlayersWithLineupModeSuccess: EventCase()
data class NewBatterOrderAvailable(val players: List<BatterState>): EventCase()
data class GetAllAvailablePlayersSuccess(val players: List<Player>, val position: FieldPosition): EventCase()
data class NeedLinkDpFlex(val initialData: Pair<Player?, Player?>, val dpLocked: Boolean, val flexLocked: Boolean, val teamType: Int, @StringRes val title: Int): EventCase()

class PlayersPositionViewModel: ViewModel(), KoinComponent {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var lineupMode = MODE_DISABLED
    var editable = false
    var teamType: Int = 0
    var strategy = TeamStrategy.STANDARD
    var extraHitters = 0

    private var team: Team? = null

    val eventHandler = PublishSubject.create<EventCase>()

    // live data observables
    private val _linkPlayersInField = MutableLiveData<List<Player>?>()
    val linkPlayersInField: LiveData<List<Player>?>
        get() = _linkPlayersInField

    private val domain: ApplicationInteractor by inject()

    private val _lineupTitle = MutableLiveData<String>()
    private val _designatedPlayerTitle = MutableLiveData<String>()
    private val _listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    private val _helpEvent = MutableLiveData(false)

    private val disposables = CompositeDisposable()

    fun clear() {
        disposables.clear()
    }

    fun onDeletePosition(player: Player, position: FieldPosition) {
        val disposable = domain.playerFieldPositions().deletePlayerPosition(player, position, _listPlayersWithPosition, lineupMode, extraHitters)
                .subscribe({
                    eventHandler.onNext(DeletePlayerPositionSuccess)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    private fun getNotSelectedPlayers(sortBy: FieldPosition? = null): Single<List<Player>> {
        return domain.lineups().getNotSelectedPlayersFromList(_listPlayersWithPosition, lineupID, sortBy)
    }

    private fun getAllAvailablePlayers(position: FieldPosition) {
        val disposable = getNotSelectedPlayers(position)
                .subscribe({
                    eventHandler.onNext(GetAllAvailablePlayersSuccess(it, position))
                }, {
                    if (it is NoSuchElementException) {
                        Timber.e(it.message)
                    } else {
                        Timber.e(it)
                    }
                })
        disposables.add(disposable)
    }

    fun getPlayerSelectionForDp() {
        val disposable = getNotSelectedPlayers()
                .subscribe({
                    _linkPlayersInField.value = it
                }, {
                    Timber.e(it)
                    _linkPlayersInField.value = listOf()
                })
        disposables.add(disposable)
    }

    fun getPlayerSelectionForFlex() {
        val disposable = domain.lineups().getPlayersInFieldFromList(_listPlayersWithPosition)
                .subscribe({
                    _linkPlayersInField.value = it
                }, {
                    if (it is NoSuchElementException) {
                        Timber.e(it.message)
                    } else {
                        Timber.e(it)
                    }
                    _linkPlayersInField.value = listOf()
                })
        disposables.add(disposable)
    }

    fun saveNewBattingOrder() {
        val disposable = domain.lineups().saveBattingOrder(_listPlayersWithPosition)
                .subscribe({
                    eventHandler.onNext(SaveBattingOrderSuccess)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    private fun deleteLineup() {
        val disposable = domain.lineups().deleteLineup(lineupID)
                .subscribe({
                    eventHandler.onNext(DeleteLineupSuccess)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    fun getTeamType(): Completable {
        return domain.teams().getTeamType().flatMapCompletable {
            this.teamType = it
            Completable.complete()
        }
    }

    fun onLineupModeChanged(isEnabled: Boolean) {
        lineupMode = if(isEnabled) MODE_ENABLED else MODE_DISABLED

        val disposable = domain.lineups().updateLineupMode(isEnabled, lineupID, lineupMode, _listPlayersWithPosition, strategy, extraHitters)
                .subscribe({
                    eventHandler.onNext(UpdatePlayersWithLineupModeSuccess)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    //TODO use case ?
    fun exportLineupToExternalStorage(context: Context, viewToSave: View?, exportType: Int): Single<Intent> {
        return Single.create(SingleOnSubscribe<ArrayList<Uri>> {
            val tmpPath = context.cacheDir.path
            val timeInMillis = Calendar.getInstance().timeInMillis

            val uris = arrayListOf<Uri>()

            viewToSave?.run {
                val filePath = when(exportType) {
                    FRAGMENT_DEFENSE_INDEX -> {
                        StringBuilder(tmpPath)
                            .append("/")
                            .append(timeInMillis).append("_defense.png").toString()
                    }
                    FRAGMENT_ATTACK_INDEX -> {
                        StringBuilder(tmpPath)
                            .append("/")
                            .append(timeInMillis).append("_attack.png").toString()
                    }
                    else -> StringBuilder(tmpPath)
                        .append("/")
                        .append(timeInMillis).append("_all.png").toString()
                }
                val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(filePath))

                val fos = FileOutputStream(filePath)
                try {
                    val success = drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                    fos.close()
                    success.takeIf { true }.let {
                        uris.add(uri)
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }

            it.onSuccess(uris)
        })
                .map {
                    val title: String = lineupTitle.toString()
                    Intent(Intent.ACTION_SEND_MULTIPLE).run {
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_lineup_subject, title))
                        type = "image/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, it)
                    }
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
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
        return domain.playerFieldPositions().switchPlayersPosition(p1, p2,_listPlayersWithPosition, lineupMode, strategy, extraHitters)
    }

    fun onPlayerSelected(player: Player, position: FieldPosition) {
        val disposable = domain.playerFieldPositions().savePlayerFieldPosition(player, position, _listPlayersWithPosition, lineupID, lineupMode, strategy, strategy.batterSize, extraHitters)
                .subscribe({
                    eventHandler.onNext(SavePlayerPositionSuccess)
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    fun onPlayerClicked(position: FieldPosition) {

        if(position != FieldPosition.DP_DH) {
            getAllAvailablePlayers(position)
        }
        else {
            val disposable = domain.lineups().getDpAndFlexFromPlayersInField(_listPlayersWithPosition)
                    .subscribe({
                        _linkPlayersInField.value = null
                        val title = if(it.teamType == TeamType.SOFTBALL.id) {
                            R.string.link_dp_and_flex_dialog_title
                        }
                        else {
                            R.string.link_dh_and_pitcher_dialog_title
                        }
                        eventHandler.onNext(NeedLinkDpFlex(Pair(it.dp, it.flex), it.dpLocked, it.flexLocked, it.teamType, title))
                    }, {
                        if (it is NeedAssignPitcherFirstException) {
                            Timber.w(it.message)
                        } else {
                            Timber.e(it)
                        }
                    })
            disposables.add(disposable)
        }
    }

    /**
     * @return boolean to indicate arguments are validated.
     */
    fun linkDpAndFlex(dp: Player?, flex: Player?): Completable {
        _linkPlayersInField.value = null
        return domain.lineups().linkDpAndFlex(dp,flex, lineupID, _listPlayersWithPosition, strategy, extraHitters)
    }

    fun getBatterStates(players: List<PlayerWithPosition>) {
        val disposable = domain.lineups().getBatterStates(players = players, teamType = teamType, batterSize = strategy.batterSize,
                extraHitterSize = extraHitters, lineupMode = lineupMode, isDebug = BuildConfig.DEBUG, isEditable = editable)
                .subscribe({
                    eventHandler.onNext(NewBatterOrderAvailable(it))
                }, {
                    Timber.e(it)
                })
        this.disposables.add(disposable)
    }

    ///////////// LIVE DATA OBSERVER //////////////

    fun registerLineupAndPositionsChanged(): LiveData<List<PlayerWithPosition>> {

        val currentLineupID = lineupID ?: 0

        val getLineup = domain.lineups().observeLineupById(currentLineupID)
        val getPositions = getLineup.switchMap {
            this.lineupMode = it.mode
            domain.lineups().observeTeamPlayersAndMaybePositionsForLineup(currentLineupID)
        }

        val getPlayerNumberOverlays = getPositions.switchMap { positions ->
            val playerMap = mutableMapOf<Long, PlayerWithPosition>()
            positions.forEach {
                playerMap[it.playerID] = it
            }
            domain.players().observePlayerNumberOverlays(currentLineupID).map {
                it.forEach {  overlay ->
                    playerMap[overlay.playerID]?.shirtNumber = overlay.number
                }
                positions
            }
        }

        return getPlayerNumberOverlays.map {
            _listPlayersWithPosition.clear()
            _listPlayersWithPosition.addAll(it)
            it
        }
    }

    fun registerLineupChange(): LiveData<Lineup> {
        return domain.lineups().observeLineupById(lineupID ?: 0)
    }

    fun getLineupName(): LiveData<String> {
        _lineupTitle.value = lineupTitle ?: ""
        return _lineupTitle
    }

    //TODO move in view
    fun getUserDeleteConsentDialog(context: Context): Dialog {
        return DialogFactory.getWarningTaskDialog(context = context,
                title = R.string.dialog_delete_lineup_title,
                message = R.string.dialog_delete_cannot_undo_message,
                task = Completable.create { emitter ->
                    deleteLineup()
                    emitter.onComplete()
                })
    }

    private fun getTeam(): Single<Team> {
        return team?.let {
            Single.just(it)
        } ?: domain.teams().getTeam().doOnSuccess {
            this.team = it
        }
    }

    fun observeLineupErrors() = domain.lineups().observeErrors()
    fun observePlayerFieldPositionErrors() = domain.playerFieldPositions().observeErrors()

    fun onTabSelected(context: Context, position: Int) {
        val prefs = context
            .getSharedPreferences(Constants.APPLICATION_PREFERENCES, Context.MODE_PRIVATE)
        val show = prefs.getBoolean(Constants.PREF_FEATURE_SHOW_REORDER_HELP, true)
        if(position == FRAGMENT_ATTACK_INDEX && editable && show) {
            prefs.edit().putBoolean(Constants.PREF_FEATURE_SHOW_REORDER_HELP, false).apply()
            _helpEvent.postValue(true)
        } else {
            _helpEvent.postValue(false)
        }
    }

    fun observeHelpEvent(): LiveData<Boolean> {
        return _helpEvent
    }
}