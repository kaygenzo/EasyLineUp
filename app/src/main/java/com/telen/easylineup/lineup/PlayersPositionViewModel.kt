package com.telen.easylineup.lineup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PointF
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
import com.telen.easylineup.*
import com.telen.easylineup.R
import com.telen.easylineup.application.App
import com.telen.easylineup.domain.*
import com.telen.easylineup.repository.model.*
import com.telen.easylineup.repository.model.FieldPosition
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.forEach
import kotlin.collections.mutableListOf

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
    UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED
}

sealed class EventCase
object SavePlayerPositionSuccess: EventCase()
object DeletePlayerPositionSuccess: EventCase()
data class GetAllAvailablePlayersSuccess(val players: List<Player>, val position: FieldPosition, val isNewPlayer: Boolean): EventCase()
object SaveBattingOrderSuccess: EventCase()
object DeleteLineupSuccess: EventCase()
object SaveLineupModeSuccess: EventCase()
object UpdatePlayersWithLineupModeSuccess: EventCase()

class PlayersPositionViewModel: ViewModel(), KoinComponent {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var lineupMode = MODE_NONE
    var editable = false

    val errorHandler = MutableLiveData<ErrorCase>()
    val eventHandler = MutableLiveData<EventCase>()

    private val listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    private val savePlayerFieldPositionUseCase: SavePlayerFieldPosition by inject()
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition by inject()
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection by inject()
    private val saveBattingOrder: SaveBattingOrder by inject()
    private val deleteLineup: DeleteLineup by inject()
    private val saveLineupMode: SaveLineupMode by inject()
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode by inject()
    private val getRoasterUseCase: GetRoaster by inject()
    private val getTeamUseCase: GetTeam by inject()

    fun savePlayerFieldPosition(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean) {

        val requestValues = SavePlayerFieldPosition.RequestValues(
                lineupID, player, position, point.x, point.y, listPlayersWithPosition, lineupMode, isNewObject)

        val disposable = UseCaseHandler.execute(savePlayerFieldPositionUseCase, requestValues).subscribe({
            eventHandler.value = SavePlayerPositionSuccess
        }, {
            errorHandler.value = ErrorCase.SAVE_PLAYER_FIELD_POSITION_FAILED
        })
    }

    fun deletePosition(player: Player, position: FieldPosition) {

        val requestValues = DeletePlayerFieldPosition.RequestValues(listPlayersWithPosition, player, position)

        val disposable = UseCaseHandler.execute(deletePlayerFieldPositionUseCase, requestValues).subscribe({
            eventHandler.value = DeletePlayerPositionSuccess
        }, {
            errorHandler.value = ErrorCase.DELETE_PLAYER_FIELD_POSITION_FAILED
        })
    }

    fun getAllAvailablePlayers(position: FieldPosition, isNewPlayer: Boolean) {
        val disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { UseCaseHandler.execute(getRoasterUseCase, GetRoaster.RequestValues(it.id, lineupID)) }
                .flatMap {
                    val requestValues = GetListAvailablePlayersForSelection.RequestValues(lineupID, listPlayersWithPosition, position, it.players)
                    UseCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
                }
                .subscribe({
                    eventHandler.value = GetAllAvailablePlayersSuccess(it.players, position, isNewPlayer)
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

    fun deleteLineup() {

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

    fun onDesignatedPlayerChanged(isEnabled: Boolean) {

        lineupMode = if(isEnabled) MODE_DH else MODE_NONE

        val disposable = saveMode().doOnError {
            errorHandler.value = ErrorCase.SAVE_LINEUP_MODE_FAILED
        }.flatMap {
            eventHandler.value = SaveLineupModeSuccess
            val requestValues = UpdatePlayersWithLineupMode.RequestValues(listPlayersWithPosition, isEnabled)
            UseCaseHandler.execute(updatePlayersWithLineupMode, requestValues)
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

            val tmpSDPath = StringBuilder(Environment.getExternalStorageDirectory().absolutePath).append("/Lineups/").toString()
            if(!File(tmpSDPath).exists())
                File(tmpSDPath).mkdir()

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
}