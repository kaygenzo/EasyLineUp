package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

interface PlayersInteractor {
    /** @deprecated **/
    fun insertPlayers(players: List<Player>): Completable
    fun getPlayer(playerID: Long): LiveData<Player>
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
        phone: String?,
        sex: Int
    ): Completable

    fun deletePlayer(playerID: Long?): Completable
    fun getPlayers(): Single<List<Player>>
    fun observePlayers(teamID: Long): LiveData<List<Player>>

    fun saveOrUpdatePlayerNumberOverlays(overlays: List<RosterItem>): Completable
    fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>>

    /** @deprecated **/
    fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun getTeamEmails(): Single<List<String>>
    fun getTeamPhones(): Single<List<String>>
    fun observePlayerNumberOverlays(lineupID: Long): LiveData<List<PlayerNumberOverlay>>
    fun observeErrors(): Subject<DomainErrors.Players>
}