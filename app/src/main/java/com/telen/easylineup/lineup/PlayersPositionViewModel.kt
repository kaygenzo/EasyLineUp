package com.telen.easylineup.lineup

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.application.App
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.*
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.utils.DialogFactory
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
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

enum class ErrorCase {
    SAVE_PLAYER_FIELD_POSITION_FAILED,
    DELETE_PLAYER_FIELD_POSITION_FAILED,
    LIST_AVAILABLE_PLAYERS_EMPTY,
    SAVE_BATTING_ORDER_FAILED,
    DELETE_LINEUP_FAILED,
    SAVE_LINEUP_MODE_FAILED,
    UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED,
    GET_TEAM_FAILED,
    NEED_ASSIGN_PITCHER_FIRST,
    DP_OR_FLEX_NOT_ASSIGNED
}

sealed class EventCase
object SavePlayerPositionSuccess: EventCase()
object DeletePlayerPositionSuccess: EventCase()
object SaveBattingOrderSuccess: EventCase()
object DeleteLineupSuccess: EventCase()
object SaveLineupModeSuccess: EventCase()
object UpdatePlayersWithLineupModeSuccess: EventCase()
data class GetAllAvailablePlayersSuccess(val players: List<Player>, val position: FieldPosition): EventCase()
data class NeedLinkDpFlex(val initialData: Pair<Player?, Player?>, val dpLocked: Boolean, val flexLocked: Boolean, val teamType: Int): EventCase()

class PlayersPositionViewModel: ViewModel(), KoinComponent {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var lineupMode = MODE_DISABLED
    var editable = false

    val errorHandler = MutableLiveData<ErrorCase>()
    val eventHandler = MutableLiveData<EventCase>()

    private val _linkPlayersInField = MutableLiveData<List<Player>?>()
    val linkPlayersInField: LiveData<List<Player>?> = _linkPlayersInField

    private val _lineupTitle = MutableLiveData<String>()

    private val _designatedPlayerTitle = MutableLiveData<String>()

    private val listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    private val savePlayerFieldPositionUseCase: SavePlayerFieldPosition by inject()
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition by inject()
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection by inject()
    private val saveBattingOrder: SaveBattingOrder by inject()
    private val deleteLineup: DeleteLineup by inject()
    private val saveLineupMode: SaveLineupMode by inject()
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode by inject()
    private val getRosterUseCase: GetRoster by inject()
    private val getTeamUseCase: GetTeam by inject()
    private val switchPlayersPositionUseCase: SwitchPlayersPosition by inject()
    private val getPlayersInField: GetOnlyPlayersInField by inject()
    private val getDpAndFlexFromPlayersInFieldUseCase: GetDPAndFlexFromPlayersInField by inject()
    private val saveDpAndFlexUseCase: SaveDpAndFlex by inject()

    private fun savePlayerFieldPosition(player: Player, position: FieldPosition): Completable {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMapCompletable {
                    val requestValues = SavePlayerFieldPosition.RequestValues(
                            lineupID = lineupID,
                            player = player,
                            position = position,
                            players = listPlayersWithPosition,
                            lineupMode = lineupMode,
                            teamType = it.type)

                    UseCaseHandler.execute(savePlayerFieldPositionUseCase, requestValues).ignoreElement()
                }
    }

    fun onDeletePosition(player: Player, position: FieldPosition) {

        val requestValues = DeletePlayerFieldPosition.RequestValues(listPlayersWithPosition, player, position, lineupMode)

        val disposable = UseCaseHandler.execute(deletePlayerFieldPositionUseCase, requestValues).subscribe({
            eventHandler.value = DeletePlayerPositionSuccess
        }, {
            errorHandler.value = ErrorCase.DELETE_PLAYER_FIELD_POSITION_FAILED
        })
    }

    private fun getNotSelectedPlayers(sortBy: FieldPosition? = null): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { UseCaseHandler.execute(getRosterUseCase, GetRoster.RequestValues(it.id, lineupID)) }
                .flatMap {
                    val requestValues = GetListAvailablePlayersForSelection.RequestValues(listPlayersWithPosition, sortBy, it.players)
                    UseCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
                }
                .map { it.players }
    }

    private fun getAllAvailablePlayers(position: FieldPosition) {
        val disposable = getNotSelectedPlayers(position)
                .subscribe({
                    eventHandler.value = GetAllAvailablePlayersSuccess(it, position)
                }, {
                    errorHandler.value = ErrorCase.LIST_AVAILABLE_PLAYERS_EMPTY
                })
    }

    fun getPlayerSelectionForDp() {

        val disposable = getNotSelectedPlayers()
                .subscribe({
                    _linkPlayersInField.value = it
                }, {
                    errorHandler.value = ErrorCase.LIST_AVAILABLE_PLAYERS_EMPTY
                })
    }

    fun getPlayerSelectionForFlex() {

        val disposable = UseCaseHandler.execute(getPlayersInField, GetOnlyPlayersInField.RequestValues(listPlayersWithPosition)).map { it.playersInField }
                .subscribe({
                    _linkPlayersInField.value = it
                }, {
                    errorHandler.value = ErrorCase.LIST_AVAILABLE_PLAYERS_EMPTY
                })
    }

    fun saveNewBattingOrder(players: List<PlayerWithPosition>) {
        val requestValues = SaveBattingOrder.RequestValues(players)

        val disposable = UseCaseHandler.execute(saveBattingOrder, requestValues).subscribe({
            eventHandler.value = SaveBattingOrderSuccess
        }, {
            errorHandler.value = ErrorCase.SAVE_BATTING_ORDER_FAILED
        })
    }

    private fun deleteLineup() {

        val requestValues = DeleteLineup.RequestValues(lineupID)

        val disposable = UseCaseHandler.execute(deleteLineup, requestValues).subscribe({
            eventHandler.value = DeleteLineupSuccess
        }, {
            errorHandler.value = ErrorCase.DELETE_LINEUP_FAILED
        })
    }

    private fun saveMode(): Single<SaveLineupMode.ResponseValue> {
        val requestValues = SaveLineupMode.RequestValues(lineupID, lineupMode)
        return UseCaseHandler.execute(saveLineupMode, requestValues)
    }

    fun onLineupModeChanged(isEnabled: Boolean) {

        lineupMode = if(isEnabled) MODE_ENABLED else MODE_DISABLED

        val disposable = saveMode().doOnError {
            errorHandler.value = ErrorCase.SAVE_LINEUP_MODE_FAILED
        }.flatMap {
            eventHandler.value = SaveLineupModeSuccess
            UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                    .flatMap {
                        val requestValues = UpdatePlayersWithLineupMode.RequestValues(listPlayersWithPosition, isEnabled, it.type)
                        UseCaseHandler.execute(updatePlayersWithLineupMode, requestValues)
                    }
        }.subscribe({
            eventHandler.value = UpdatePlayersWithLineupModeSuccess
        }, {
            errorHandler.value = ErrorCase.UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED
        })
    }

    //TODO use case ?
    fun exportLineupToExternalStorage(context: Context, map: Map<Int, Fragment>): Single<Intent> {
        return Single.create(SingleOnSubscribe<ArrayList<Uri>> {

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                it.onError(InsufficientPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)))
                return@SingleOnSubscribe
            }

            val tmpSDPath = StringBuilder(Environment.getExternalStorageDirectory().absolutePath).append("/${Constants.LINEUPS_DIRECTORY}").toString()
            if(!File(tmpSDPath).exists())
                File(tmpSDPath).mkdirs()

            val timeInMillis = Calendar.getInstance().timeInMillis

            val uris = arrayListOf<Uri>()

            map.forEach {
                val filePath = when(it.key) {
                    FRAGMENT_DEFENSE_INDEX -> {
                        StringBuilder(tmpSDPath).append(timeInMillis).append("_defense.png").toString()
                    }
                    FRAGMENT_ATTACK_INDEX -> {
                        StringBuilder(tmpSDPath).append(timeInMillis).append("_attack.png").toString()
                    }
                    else -> StringBuilder(tmpSDPath).append(timeInMillis).append("unknown.png").toString()
                }
                val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(filePath))

                val fos = FileOutputStream(filePath)
                try {
                    val success = it.value.view?.drawToBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                    fos.close()
                    success?.takeIf { true }.let {
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
        val position1 = FieldPosition.getFieldPosition(p1.position) ?: FieldPosition.FIRST_BASE
        val position2 = FieldPosition.getFieldPosition(p2.position) ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    fun switchPlayersPosition(p1: PlayerWithPosition, position2: FieldPosition): Completable {
        val position1 = FieldPosition.getFieldPosition(p1.position) ?: FieldPosition.FIRST_BASE
        return switchPlayersPosition(position1, position2)
    }

    private fun switchPlayersPosition(p1: FieldPosition, p2: FieldPosition): Completable {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMapCompletable {
                    UseCaseHandler.execute(switchPlayersPositionUseCase, SwitchPlayersPosition.RequestValues(
                            players = listPlayersWithPosition,
                            position1 = p1,
                            position2 = p2,
                            teamType = it.type,
                            lineupMode = lineupMode
                    )).ignoreElement()
                }
    }

    fun onPlayerSelected(player: Player, position: FieldPosition) {
        val disposable = savePlayerFieldPosition(player, position)
                .subscribe({
                    eventHandler.value = SavePlayerPositionSuccess
                }, {
                    errorHandler.value = ErrorCase.SAVE_PLAYER_FIELD_POSITION_FAILED
                })
    }

    fun onPlayerClicked(position: FieldPosition) {

        if(position != FieldPosition.DP_DH) {
            getAllAvailablePlayers(position)
        }
        else {
            val disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                    .flatMap {
                        UseCaseHandler.execute(getDpAndFlexFromPlayersInFieldUseCase, GetDPAndFlexFromPlayersInField.RequestValues(listPlayersWithPosition, it.type))
                    }
                    .subscribe({
                        _linkPlayersInField.value = null
                        eventHandler.value = NeedLinkDpFlex(Pair(it.dp, it.flex), it.dpLocked, it.flexLocked, it.teamType)
                    }, {
                        if(it is NeedAssignPitcherFirstException) {
                            errorHandler.value = ErrorCase.NEED_ASSIGN_PITCHER_FIRST
                        }
                        else {
                            errorHandler.value = ErrorCase.GET_TEAM_FAILED
                        }
                    })
        }
    }

    /**
     * @return boolean to indicate arguments are validated.
     */
    fun linkDpAndFlex(dp: Player?, flex: Player?): Completable {
        _linkPlayersInField.value = null
        return UseCaseHandler.execute(saveDpAndFlexUseCase, SaveDpAndFlex.RequestValues(
                lineupID = lineupID, dp = dp, flex = flex, players = listPlayersWithPosition
        ))
                .ignoreElement()
                .doOnError {
                    if(it is NeedAssignBothPlayersException) {
                        errorHandler.postValue(ErrorCase.DP_OR_FLEX_NOT_ASSIGNED)
                    }
                }
    }

    /**
     * @return @LayoutRes of the layout
     */
    fun getLayout(): Int {
        return when(editable) {
            true -> R.layout.fragment_lineup_edition
            false -> R.layout.fragment_lineup_fixed
        }
    }

    ///////////// LIVE DATA OBSERVER //////////////

    fun registerLineupAndPositionsChanged(): LiveData<List<PlayerWithPosition>> {

        val getLineup = App.database.lineupDao().getLineupById(lineupID ?: 0)
        val getPositions = Transformations.switchMap(getLineup) {
            this.lineupMode = it?.mode ?: 0
            App.database.playerDao().getTeamPlayersAndMaybePositions(it?.id ?: 0)
        }

        return Transformations.map(getPositions) {
            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(it)
            it
        }
    }

    fun registerLineupBatters(): LiveData<List<PlayerWithPosition>> {
        return Transformations.map(registerLineupAndPositionsChanged()) { players ->
            players.filter { it.order > 0 }
                    .sortedBy { it.order }
        }
    }

    fun registerLineupChange(): LiveData<Lineup> {
        return App.database.lineupDao().getLineupById(lineupID ?: 0)
    }

    fun getLineupName(): LiveData<String> {
        _lineupTitle.value = lineupTitle ?: ""
        return _lineupTitle
    }

    fun getDesignatedPlayerLabel(context: Context): LiveData<String> {
        val disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .subscribe({
                    _designatedPlayerTitle.value = when(it.team.type) {
                        TeamType.SOFTBALL.id -> context.getString(R.string.action_add_dp_flex)
                        else -> context.getString(R.string.action_add_dh)
                    }
                }, {
                    errorHandler.value = ErrorCase.GET_TEAM_FAILED
                })
        return _designatedPlayerTitle
    }

    //TODO move in view
    fun getUserDeleteConsentDialog(context: Context): Dialog {
        return DialogFactory.getWarningDialog(context,
                context.getString(R.string.dialog_delete_lineup_title),
                context.getString(R.string.dialog_delete_cannot_undo_message),
                Completable.create { emitter ->
                    deleteLineup()
                    emitter.onComplete()
                })
    }
}