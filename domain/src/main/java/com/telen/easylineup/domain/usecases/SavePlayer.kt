/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.net.Uri
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import io.reactivex.rxjava3.core.Completable

/**
 * Validates the player, then inserts/updates it under the current team.
 */
class SavePlayer(
    private val dao: PlayerRepository,
    private val getTeam: GetTeam,
    private val validatorUtils: ValidatorUtils,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(
        playerId: Long,
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long? = 0,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    ): Completable {
        return when {
            name.isNullOrBlank() -> Completable.error(NameEmptyException())
            !validatorUtils.isEmailValid(email) -> Completable.error(InvalidEmailException())
            !validatorUtils.isValidPhoneNumber(phone) -> Completable.error(InvalidPhoneException())
            else -> getTeam().flatMapCompletable { team ->
                val player = Player(
                    id = playerId,
                    teamId = team.id,
                    name = name.trim(),
                    shirtNumber = shirtNumber ?: 0,
                    licenseNumber = licenseNumber ?: 0L,
                    image = imageUri?.toString(),
                    positions = positions,
                    pitching = pitching,
                    batting = batting,
                    email = email,
                    phone = phone,
                    sex = sex
                )

                if (player.id == 0L) {
                    dao.insertPlayer(player).ignoreElement()
                } else {
                    dao.getPlayerByIdAsSingle(player.id).flatMapCompletable {
                        player.hash = it.hash
                        dao.updatePlayer(player)
                    }
                }
            }
        }.subscribeOn(schedulersProvider.io())
    }
}
