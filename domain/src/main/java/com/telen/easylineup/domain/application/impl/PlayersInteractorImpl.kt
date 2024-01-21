/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.PlayersInteractor
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.GetPlayer
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.SavePlayerNumberOverlay
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class PlayersInteractorImpl : PlayersInteractor, KoinComponent {
    private val playersRepo: PlayerRepository by inject()
    private val getPlayer: GetPlayer by inject()
    private val deletePlayer: DeletePlayer by inject()
    private val savePlayer: SavePlayer by inject()
    private val getPlayerPositionsSummary: GetPositionsSummaryForPlayer by inject()
    private val getPlayers: GetPlayers by inject()
    private val getTeam: GetTeam by inject()
    private val savePlayerNumberOverlay: SavePlayerNumberOverlay by inject()
    private val getShirtNumberHistory: GetShirtNumberHistory by inject()
    private val validatorUtils: ValidatorUtils by inject()
    private val errors: PublishSubject<DomainErrors.Players> = PublishSubject.create()

    override fun insertPlayers(players: List<Player>): Completable {
        return playersRepo.insertPlayers(players)
    }

    override fun getPlayer(playerId: Long): LiveData<Player> {
        return playersRepo.getPlayerById(playerId)
    }

    override fun getPlayerPositionsSummary(playerId: Long?): Single<Map<FieldPosition, Int>> {
        val request = GetPositionsSummaryForPlayer.RequestValues(playerId)
        return UseCaseHandler.execute(getPlayerPositionsSummary, request)
            .map { it.summary }
    }

    override fun savePlayer(
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
    ): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMapCompletable {
                val req = SavePlayer.RequestValues(
                    validatorUtils,
                    playerId ?: 0,
                    it.id,
                    name,
                    shirtNumber,
                    licenseNumber,
                    imageUri,
                    positions,
                    pitching,
                    batting,
                    email,
                    phone,
                    sex
                )
                UseCaseHandler.execute(savePlayer, req).ignoreElement()
            }
            .doOnError {
                when (it) {
                    is NameEmptyException ->
                        errors.onNext(DomainErrors.Players.INVALID_PLAYER_NAME)
                    is InvalidEmailException ->
                        errors.onNext(DomainErrors.Players.INVALID_EMAIL_FORMAT)
                    is InvalidPhoneException ->
                        errors.onNext(DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT)
                }
            }
    }

    override fun deletePlayer(playerId: Long?): Completable {
        return UseCaseHandler.execute(getPlayer, GetPlayer.RequestValues(playerId))
            .map { it.player }
            .flatMap { player ->
                UseCaseHandler.execute(deletePlayer, DeletePlayer.RequestValues(player))
            }
            .ignoreElement()
    }

    override fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap { team ->
                UseCaseHandler.execute(getPlayers, GetPlayers.RequestValues(team.id))
            }
            .map { it.players }
    }

    override fun observePlayers(teamId: Long): LiveData<List<Player>> {
        return playersRepo.observePlayers(teamId)
    }

    override fun saveOrUpdatePlayerNumberOverlays(overlays: List<RosterItem>): Completable {
        return UseCaseHandler
            .execute(savePlayerNumberOverlay, SavePlayerNumberOverlay.RequestValues(overlays))
            .ignoreElement()
    }

    override fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = GetShirtNumberHistory.RequestValues(it.id, number)
                UseCaseHandler.execute(getShirtNumberHistory, request)
            }
            .map { it.history }
    }

    override fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable {
        return playersRepo.createPlayerNumberOverlays(overlays)
    }

    override fun getTeamEmails(): Single<List<String>> {
        return getPlayers().map { players ->
            players
                .filter { player -> !TextUtils.isEmpty(player.email) }
                .map { it.email ?: "" }
        }
    }

    override fun getTeamPhones(): Single<List<String>> {
        return getPlayers().map { players ->
            players
                .filter { player -> !TextUtils.isEmpty(player.phone) }
                .map { it.phone ?: "" }
        }
    }

    override fun observePlayerNumberOverlays(lineupId: Long): LiveData<List<PlayerNumberOverlay>> {
        return playersRepo.observePlayersNumberOverlay(lineupId)
    }

    override fun observeErrors(): Subject<DomainErrors.Players> {
        return errors
    }
}
