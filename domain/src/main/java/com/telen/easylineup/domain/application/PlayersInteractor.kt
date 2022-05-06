package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

interface PlayersInteractor {
    /** @deprecated **/
    fun insertPlayers(players: List<Player>): Completable
    fun getPlayer(playerID: Long?): Single<Player>
    fun getPlayerPositionsSummary(playerID: Long?): Single<Map<FieldPosition, Int>>
    fun savePlayer(
        playerID: Long?,
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?
    ): Completable

    fun deletePlayer(playerID: Long?): Completable
    fun getPlayers(): Single<List<Player>>

    fun saveOrUpdatePlayerNumberOverlays(overlays: List<RosterItem>): Completable
    fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>>

    /** @deprecated **/
    fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun getTeamEmails(): Single<List<String>>
    fun getTeamPhones(): Single<List<String>>
    fun observePlayerNumberOverlays(lineupID: Long): LiveData<List<PlayerNumberOverlay>>
    fun observeErrors(): Subject<DomainErrors.Players>
}