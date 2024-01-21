/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import android.content.Context
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.utils.getPositionShortNames
import io.reactivex.rxjava3.core.Single

internal class GetBattersState :
    UseCase<GetBattersState.RequestValues, GetBattersState.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val positionDescriptions =
            getPositionShortNames(requestValues.context, requestValues.teamType)
        val result: MutableList<BatterState> = mutableListOf()
        val maxBatterSize = requestValues.batterSize + requestValues.extraHitterSize

        var position = 0
        var subsFoundNumber = 0
        requestValues.players
            .filter { it.order > 0 }
            .sortedBy { it.order }
            .forEach { player ->
                val playerFlag = player.flags
                val playerId = player.playerId
                val isSubstitute = player.isSubstitute()
                val isDefensePlayer = player.isDefensePlayer()

                var canMove = false
                var canShowDescription = false
                var canShowIndex = false
                var canShowPosition = isDefensePlayer
                var applyBackground = false

                val order = player.order
                val playerName = player.playerName.trim()
                var playerPositionDesc = ""
                val shirtNumber = player.shirtNumber.toString()

                var isDp = false
                var isFlex = false

                when {
                    player.isSubstitute() -> {
                        subsFoundNumber++
                        if (position < maxBatterSize) {
                            canShowIndex = subsFoundNumber <= requestValues.extraHitterSize
                        }
                    }
                    player.isDpDh() -> {
                        isDp = true
                        canShowIndex = true
                    }
                    else -> {
                        isFlex = player.isFlex()
                        if (isFlex) {
                            applyBackground = true
                        } else {
                            canShowIndex = true
                        }
                    }
                }

                if (requestValues.isDebug) {
                    canShowIndex = true
                }

                if (player.position >= 0) {
                    playerPositionDesc = positionDescriptions[player.position]
                }

                if (!requestValues.isEditable) {
                    canShowDescription = true
                } else if (position < maxBatterSize) {
                    if (!isFlex) {
                        canMove = true
                    }
                    if (isDp) {
                        canShowDescription = true
                    }
                    // In case of substitutes are added before defense ones, let's prevent non
                    // authorized ones to be moved
                    if (subsFoundNumber > requestValues.extraHitterSize) {
                        canMove = false
                    }
                } else {
                    canShowDescription = true
                }

                if (isSubstitute) {
                    canShowPosition = false
                    canShowDescription = true
                }

                // do not show field position value for baseball 5
                if (requestValues.teamType == TeamType.BASEBALL_5.id) {
                    canShowPosition = false
                }

                position++

                result.add(
                    BatterState(
                        playerId,
                        playerFlag,
                        order,
                        playerName,
                        shirtNumber,
                        FieldPosition.getFieldPositionById(player.position)
                            ?: FieldPosition.SUBSTITUTE,
                        playerPositionDesc,
                        canShowPosition,
                        canMove,
                        canShowDescription,
                        canShowIndex,
                        applyBackground,
                        requestValues.isEditable
                    )
                )
            }

        return Single.just(ResponseValue(result))
    }

    /**
     * @property players
     */
    class ResponseValue(val players: List<BatterState>) : UseCase.ResponseValue
    /**
     * @property context
     * @property players
     * @property teamType
     * @property batterSize
     * @property extraHitterSize
     * @property isDebug
     * @property isEditable
     */
    class RequestValues(
        val context: Context, val players: List<PlayerWithPosition>, val teamType: Int,
        val batterSize: Int, val extraHitterSize: Int,
        val isDebug: Boolean, val isEditable: Boolean
    ) : UseCase.RequestValues
}
