/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.ShirtNumberEntry
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

interface PlayersInteractor {
    /** @deprecated
     * @return **/
    fun insertPlayers(players: List<Player>): Completable
    fun getPlayer(playerId: Long): LiveData<Player>
    fun getPlayerPositionsSummary(playerId: Long?): Single<Map<FieldPosition, Int>>
    fun savePlayer(
        playerId: Long?,
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

    fun deletePlayer(playerId: Long?): Completable
    fun getPlayers(): Single<List<Player>>
    fun observePlayers(teamId: Long): LiveData<List<Player>>

    fun saveOrUpdatePlayerNumberOverlays(overlays: List<RosterItem>): Completable
    fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>>

    /** @deprecated
     * @return **/
    fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun getTeamEmails(): Single<List<String>>
    fun getTeamPhones(): Single<List<String>>
    fun observePlayerNumberOverlays(lineupId: Long): LiveData<List<PlayerNumberOverlay>>
    fun observeErrors(): Subject<DomainErrors.Players>
}
