package com.telen.easylineup.domain

import android.net.Uri
import com.telen.easylineup.repository.data.PlayerDao
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single

class ShirtNumberEmptyException: Exception()
class LicenseNumberEmptyException: Exception()

class SavePlayer(val dao: PlayerDao): UseCase<SavePlayer.RequestValues, SavePlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return when {
            requestValues.name.isNullOrBlank() -> Single.error(NameEmptyException())
            requestValues.shirtNumber == null -> Single.error(ShirtNumberEmptyException())
            requestValues.licenseNumber == null -> Single.error(LicenseNumberEmptyException())
            else -> {
                val player = Player(id = requestValues.playerID, teamId = requestValues.teamID, name = requestValues.name.trim(), shirtNumber = requestValues.shirtNumber,
                        licenseNumber = requestValues.licenseNumber, image = requestValues.imageUri?.toString(), positions = requestValues.positions)

                val task = if(player.id == 0L) {
                    dao.insertPlayer(player).ignoreElement()
                }
                else {
                    dao.updatePlayer(player)
                }

                task.andThen(Single.just(ResponseValue()))
            }
        }
    }

    class RequestValues(val playerID: Long,
                              val teamID: Long,
                              val name: String?,
                              val shirtNumber: Int?,
                              val licenseNumber: Long?,
                              val imageUri: Uri?,
                              val positions: Int
    ): UseCase.RequestValues

    class ResponseValue: UseCase.ResponseValue
}