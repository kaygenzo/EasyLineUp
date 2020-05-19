package com.telen.easylineup.domain.usecases

import android.net.Uri
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.ShirtNumberEmptyException
import io.reactivex.Single

internal class SavePlayer(val dao: PlayerRepository): UseCase<SavePlayer.RequestValues, SavePlayer.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return when {
            requestValues.name.isNullOrBlank() -> Single.error(NameEmptyException())
            requestValues.shirtNumber == null -> Single.error(ShirtNumberEmptyException())
            else -> {
                val player = Player(id = requestValues.playerID, teamId = requestValues.teamID, name = requestValues.name.trim(), shirtNumber = requestValues.shirtNumber,
                        licenseNumber = requestValues.licenseNumber ?: 0L, image = requestValues.imageUri?.toString(), positions = requestValues.positions,
                        pitching = requestValues.pitching, batting = requestValues.batting
                )

                val task = if(player.id == 0L) {
                    dao.insertPlayer(player).ignoreElement()
                }
                else {
                    dao.getPlayerByIdAsSingle(player.id).flatMapCompletable {
                        player.hash = it.hash
                        dao.updatePlayer(player)
                    }
                }

                task.andThen(Single.just(ResponseValue()))
            }
        }
    }

    class RequestValues(val playerID: Long,
                        val teamID: Long,
                        val name: String?,
                        val shirtNumber: Int?,
                        val licenseNumber: Long? = 0,
                        val imageUri: Uri?,
                        val positions: Int,
                        val pitching: Int,
                        val batting: Int
    ): UseCase.RequestValues

    class ResponseValue: UseCase.ResponseValue
}