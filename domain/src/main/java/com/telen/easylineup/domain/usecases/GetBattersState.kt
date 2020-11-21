package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import io.reactivex.Single

data class BatterState(val playerID: Long, val playerFlag: Int, var playerOrder: Int, val playerName: String, val playerNumber: String,
                       val playerPosition: FieldPosition, val playerPositionDesc: String, val canShowPosition: Boolean,
                       val canMove: Boolean, val canShowDescription: Boolean, val canShowOrder: Boolean)

internal class GetBattersState: UseCase<GetBattersState.RequestValues, GetBattersState.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {

        val positionDescriptions = FieldPosition.getPositionShortNames(requestValues.context, requestValues.teamType)
        val result = mutableListOf<BatterState>()
        val maxBatterSize = requestValues.batterSize + requestValues.extraHitterSize

        var position = 0
        requestValues.players.forEach { player ->
            val playerFlag = player.flags
            val playerID = player.playerID
            val fieldPosition = FieldPosition.getFieldPositionById(player.position)
            val isSubstitute = FieldPosition.isSubstitute(player.position)
            val isDefensePlayer = FieldPosition.isDefensePlayer(player.position)

            var canMove = false
            var canShowDescription = false
            var canShowIndex = false
            var canShowPosition = isDefensePlayer

            val order = player.order
            val playerName = player.playerName.trim()
            var playerPositionDesc = ""
            val shirtNumber = player.shirtNumber.toString()

            var isDP = false
            var isFlex = false

            fieldPosition?.run {
                when (this) {
                    FieldPosition.SUBSTITUTE -> {
                        //Log.d("", "id=${position} position=${fieldPosition} maxSize=${maxBatterSize}")
                        if (position < maxBatterSize) {
                            canShowIndex = true
                        }
                    }
                    FieldPosition.DP_DH -> {
                        isDP = requestValues.lineupMode == MODE_ENABLED
                    }
                    else -> {
                        isFlex = requestValues.lineupMode == MODE_ENABLED && (player.flags and PlayerFieldPosition.FLAG_FLEX > 0)
                        canShowIndex = true
                    }
                }

                if (requestValues.isDebug) {
                    canShowIndex = true
                }

                playerPositionDesc = positionDescriptions[this@run.ordinal]
            }

            if (!requestValues.isEditable) {
                canShowDescription = true
            } else if (position < maxBatterSize) {
                if(!isFlex)
                    canMove = true
                if(isDP) {
                    canShowDescription = true
                }
            }
            else {
                canShowDescription = true
            }

            if(isSubstitute) {
                canShowPosition = false
                canShowDescription = true
            }

            position++

            result.add(BatterState(playerID, playerFlag, order, playerName, shirtNumber, fieldPosition ?: FieldPosition.SUBSTITUTE,
                    playerPositionDesc, canShowPosition, canMove, canShowDescription, canShowIndex))
        }

        return Single.just(ResponseValue(result))
    }

    class ResponseValue(val players: List<BatterState>): UseCase.ResponseValue
    class RequestValues(val context: Context, val players: List<PlayerWithPosition>, val teamType: Int,
                        val batterSize: Int, val extraHitterSize: Int, val lineupMode: Int,
                        val isDebug: Boolean, val isEditable: Boolean): UseCase.RequestValues
}