/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

/**
 * Validates the player, then inserts/updates it under the current team.
 */
class SavePlayer(
    private val dao: PlayerRepository,
    private val getTeam: GetTeam,
    private val validatorUtils: ValidatorUtils,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        playerId: Long,
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long? = 0,
        image: String?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    ): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            if (name.isNullOrBlank()) {
                throw NameEmptyException()
            }
            if (!validatorUtils.isEmailValid(email)) {
                throw InvalidEmailException()
            }
            if (!validatorUtils.isValidPhoneNumber(phone)) {
                throw InvalidPhoneException()
            }

            val team = getTeam().await()
            val player = Player(
                id = playerId,
                teamId = team.id,
                name = name.trim(),
                shirtNumber = shirtNumber ?: 0,
                licenseNumber = licenseNumber ?: 0L,
                image = image,
                positions = positions,
                pitching = pitching,
                batting = batting,
                email = email,
                phone = phone,
                sex = sex
            )

            if (player.id == 0L) {
                dao.insertPlayer(player).await()
            } else {
                val existing = dao.getPlayerByIdAsSingle(player.id).await()
                player.hash = existing.hash
                dao.updatePlayer(player).await()
            }
        }
    }
}
