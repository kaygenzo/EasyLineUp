package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import io.reactivex.Single

internal class GetBattersState: UseCase<GetBattersState.RequestValues, GetBattersState.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {

        val positionDescriptions = FieldPosition.getPositionShortNames(requestValues.context, requestValues.teamType)
        val result = mutableListOf<BatterState>()
        val maxBatterSize = requestValues.batterSize + requestValues.extraHitterSize

        var position = 0
        var subsFoundNumber = 0
        requestValues.players
                .filter { it.order > 0 }
                .sortedBy { it.order }
                .forEach { player ->
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
                                subsFoundNumber++
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
                        // In case of substitues are added before defense ones, let's prevent non authorized ones to be moved
                        if(subsFoundNumber > requestValues.extraHitterSize) {
                            canMove = false
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
                            playerPositionDesc, canShowPosition, canMove, canShowDescription, canShowIndex, player))
                }

        return Single.just(ResponseValue(result))
    }

    class ResponseValue(val players: List<BatterState>): UseCase.ResponseValue
    class RequestValues(val context: Context, val players: List<PlayerWithPosition>, val teamType: Int,
                        val batterSize: Int, val extraHitterSize: Int, val lineupMode: Int,
                        val isDebug: Boolean, val isEditable: Boolean): UseCase.RequestValues
}