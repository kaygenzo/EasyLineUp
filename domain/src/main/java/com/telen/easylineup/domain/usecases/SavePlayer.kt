/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.net.Uri
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import io.reactivex.rxjava3.core.Single

/**
 * @property dao
 */
internal class SavePlayer(val dao: PlayerRepository) :
    UseCase<SavePlayer.RequestValues, SavePlayer.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return when {
            requestValues.name.isNullOrBlank() -> Single.error(NameEmptyException())
            !requestValues.validatorUtils.isEmailValid(requestValues.email) -> Single.error(
                InvalidEmailException()
            )
            !requestValues.validatorUtils.isValidPhoneNumber(requestValues.phone) -> Single.error(
                InvalidPhoneException()
            )
            else -> {
                val player = Player(
                    id = requestValues.playerId,
                    teamId = requestValues.teamId,
                    name = requestValues.name.trim(),
                    shirtNumber = requestValues.shirtNumber ?: 0,
                    licenseNumber = requestValues.licenseNumber ?: 0L,
                    image = requestValues.imageUri?.toString(),
                    positions = requestValues.positions,
                    pitching = requestValues.pitching,
                    batting = requestValues.batting,
                    email = requestValues.email,
                    phone = requestValues.phone,
                    sex = requestValues.sex
                )

                val task = if (player.id == 0L) {
                    dao.insertPlayer(player).ignoreElement()
                } else {
                    dao.getPlayerByIdAsSingle(player.id).flatMapCompletable {
                        player.hash = it.hash
                        dao.updatePlayer(player)
                    }
                }

                task.andThen(Single.just(ResponseValue()))
            }
        }
    }

    /**
     * @property validatorUtils
     * @property playerId
     * @property teamId
     * @property name
     * @property shirtNumber
     * @property licenseNumber
     * @property imageUri
     * @property positions
     * @property pitching
     * @property batting
     * @property email
     * @property phone
     * @property sex
     */
    class RequestValues(
        val validatorUtils: ValidatorUtils,
        val playerId: Long,
        val teamId: Long,
        val name: String?,
        val shirtNumber: Int?,
        val licenseNumber: Long? = 0,
        val imageUri: Uri?,
        val positions: Int,
        val pitching: Int,
        val batting: Int,
        val email: String?,
        val phone: String?,
        val sex: Int
    ) : UseCase.RequestValues

    class ResponseValue : UseCase.ResponseValue
}
