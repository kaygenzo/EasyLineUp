/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.StringResourcesProvider
import kotlinx.coroutines.withContext

class GetBattersState(
    private val stringResourcesProvider: StringResourcesProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        teamType: Int,
        batterSize: Int,
        extraHitterSize: Int,
        isDebug: Boolean,
        isEditable: Boolean
    ): Result<List<BatterState>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val positionDescriptions = stringResourcesProvider.positionShortNames(teamType)
            val result: MutableList<BatterState> = mutableListOf()
            val maxBatterSize = batterSize + extraHitterSize

            var position = 0
            var subsFoundNumber = 0
            players
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
                                canShowIndex = subsFoundNumber <= extraHitterSize
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

                    if (isDebug) {
                        canShowIndex = true
                    }

                    if (player.position >= 0) {
                        playerPositionDesc = positionDescriptions[player.position]
                    }

                    if (!isEditable) {
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
                        if (subsFoundNumber > extraHitterSize) {
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
                    if (teamType == TeamType.BASEBALL_5.id) {
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
                            isEditable
                        )
                    )
                }

            result as List<BatterState>
        }
    }
}
