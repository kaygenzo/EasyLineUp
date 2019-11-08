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
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

data class LineupStatusDefense(val players: Map<Player, FieldPosition?>, val lineupMode: Int)
data class LineupStatusAttack(val players: Map<Player, FieldPosition?>, val lineupMode: Int)

const val ORDER_PITCHER_WHEN_DH = 10

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

class PlayersPositionViewModel: ViewModel() {

    var lineupID: Long? = 0
    var lineupTitle: String? = null
    var lineupMode = MODE_NONE
    var editable = false
    val listPlayersWithPosition: MutableList<PlayerWithPosition> = mutableListOf()

    val teams: LiveData<List<Team>> = App.database.teamDao().getTeams()

    private fun getNextAvailableOrder(): Int {
        var availableOrder = 1
        listPlayersWithPosition
                .filter { it.fieldPositionID > 0 }
                .sortedBy { it.order }
                .forEach {
                    if(it.order == availableOrder)
                        availableOrder++
                    else
                        return availableOrder
                }
        return availableOrder
    }

    fun savePlayerFieldPosition(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean): Completable {

        lineupID?.let { lineupID ->
            val playerPosition: PlayerFieldPosition = if(isNewObject) {
                val order = when(position) {
                    FieldPosition.SUBSTITUTE -> 200
                    FieldPosition.PITCHER -> {
                        if(lineupMode == MODE_NONE)
                            getNextAvailableOrder()
                        else
                            ORDER_PITCHER_WHEN_DH
                    }
                    else -> getNextAvailableOrder()
                }
                PlayerFieldPosition(
                        playerId = player.id,
                        lineupId = lineupID,
                        position = position.position,
                        order = order)
            } else {
                listPlayersWithPosition.first { it.position == position.position }.toPlayerFieldPosition()
            }

            playerPosition.apply {
                playerId = player.id
                x = point.x
                y = point.y
            }

            Timber.d("before playerFieldPosition=$playerPosition")

            return if(playerPosition.id > 0) {
                App.database.lineupDao().updatePlayerFieldPosition(playerPosition)
            } else {
                App.database.lineupDao().insertPlayerFieldPosition(playerPosition).ignoreElement()
            }
        } ?: return Completable.error(IllegalStateException("LineupID is null"))
    }

    fun deletePosition(position: FieldPosition): Completable {
        try {
            listPlayersWithPosition.first { it.position == position.position }.let {
                return App.database.lineupDao().deletePosition(it.toPlayerFieldPosition())
            }
        }
        catch (e: NoSuchElementException) {
            return Completable.error(e)
        }
    }

    fun deletePosition(player: Player, position: FieldPosition): Completable {
        try {
            listPlayersWithPosition.first { it.playerID == player.id && it.position == position.position }.let {
                return App.database.lineupDao().deletePosition(it.toPlayerFieldPosition())
            }
        }
        catch (e: NoSuchElementException) {
            return Completable.error(e)
        }
    }

    fun saveNewBattingOrder(playerPosition: MutableList<PlayerWithPosition>): Completable {
        val listOperations: MutableList<Completable> = mutableListOf()
        playerPosition.forEach {player ->
            listOperations.add(App.database.lineupDao().getPlayerFieldPosition(player.fieldPositionID)
                    .flatMapCompletable { playerPosition ->
                        playerPosition.order = player.order
                        App.database.lineupDao().updatePlayerFieldPosition(playerPosition)
                    }
            )
        }
        return Completable.concat(listOperations)
    }

    fun getPlayersWithPositions(lineupID: Long): LiveData<List<PlayerWithPosition>> {
        return Transformations.map(App.database.lineupDao().getAllPlayersWithPositionsForLineup(lineupID)) {
            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(it)
            it
        }

    }

    fun getTeamPlayerWithPositions(lineupID: Long): LiveData<LineupStatusDefense> {

        val getLineup = App.database.lineupDao().getLineupById(lineupID)

        val getListPlayersLiveData = Transformations.switchMap(getLineup) {
            this.lineupMode = it.mode
            App.database.playerDao().getTeamPlayersWithPositions(lineupID)
        }

        return Transformations.map(getListPlayersLiveData) { players ->

            listPlayersWithPosition.clear()
            listPlayersWithPosition.addAll(players)

            val result: MutableMap<Player, FieldPosition?> = mutableMapOf()
            players.forEach {
                var position: FieldPosition? = null
                if(it.fieldPositionID > 0) {
                    position = FieldPosition.getFieldPosition(it.position)
                }
                val player = it.toPlayer()
                result[player] = position
            }
            LineupStatusDefense(result, lineupMode)
        }
    }

    fun deleteLineup(): Completable {
        lineupID?.let { id ->
            return App.database.lineupDao().getLineupByIdSingle(id)
                    .flatMapCompletable { lineup -> App.database.lineupDao().deleteLineup(lineup) }
        } ?: return Completable.complete()
    }

    private fun saveMode(): Completable {
        lineupID?.let { id ->
            return App.database.lineupDao().getLineupByIdSingle(id)
                    .flatMapCompletable { lineup ->
                        lineup.mode = lineupMode
                        App.database.lineupDao().updateLineup(lineup)
                    }
        } ?: return Completable.complete()
    }

    fun registerLineupChange(): LiveData<Lineup> {
        return App.database.lineupDao().getLineupById(lineupID ?: 0)
    }

    fun onDesignatedPlayerChanged(isEnabled: Boolean) {
        val task = Single.just(isEnabled)
                .flatMapCompletable { isDesignatedPlayerEnabled ->
                    this.lineupMode = if(isEnabled) MODE_DH else MODE_NONE
                    val playerTask: Completable = when(isDesignatedPlayerEnabled) {
                        true -> {
                            listPlayersWithPosition.filter { it.position == FieldPosition.PITCHER.position }.firstOrNull()?.let {
                                val playerFieldPosition = it.toPlayerFieldPosition()
                                playerFieldPosition.order = ORDER_PITCHER_WHEN_DH
                                App.database.lineupDao().updatePlayerFieldPosition(playerFieldPosition)
                            } ?: Completable.complete()
                        }
                        false -> {
                            listPlayersWithPosition.filter {
                                it.position == FieldPosition.DH.position || it.position == FieldPosition.PITCHER.position
                            }.let { list ->
                                Observable.fromIterable(list).flatMapCompletable { playerPosition ->
                                    FieldPosition.getFieldPosition(playerPosition.position)?.let {
                                        deletePosition(it)
                                    }
                                }
                            } ?: Completable.complete()
                        }
                    }
                    saveMode().andThen(playerTask)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {
                    Timber.e(it)
                })
    }

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
                val success = it.value.view?.drawToBitmap()?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                success?.takeIf { true }.let {
                    uris.add(uri)
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
}