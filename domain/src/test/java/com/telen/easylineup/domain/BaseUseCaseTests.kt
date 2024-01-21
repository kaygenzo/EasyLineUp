/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isPitcher

open class BaseUseCaseTests {
    private fun generatePlayerWithPosition(
        id: Long,
        teamId: Long,
        position: FieldPosition?,
        flag: Int,
        order: Int,
        lineupId: Long,
        positions: Int = 1
    ): PlayerWithPosition {
        return PlayerWithPosition(
            playerName = "player${id.toInt()}",
            shirtNumber = id.toInt(),
            licenseNumber = id,
            teamId = teamId,
            image = null,
            position = position?.id ?: -1,
            x = 0f, y = 0f,
            flags = flag,
            order = order,
            fieldPositionId = id,
            playerId = id,
            lineupId = lineupId,
            playerPositions = positions,
            playerSex = 0
        )
    }

    fun generate(
        id: Long,
        pos: FieldPosition?,
        flag: Int,
        order: Int,
        positions: Int = 1
    ): PlayerWithPosition {
        return generatePlayerWithPosition(id, 1, pos, flag, order, 1, positions)
    }

    fun generateRosterPlayerStatus(
        id: Long,
        positions: Int,
        status: Boolean,
        teamId: Long = 1,
        playerNumberOverlay: PlayerNumberOverlay? = null
    ): RosterPlayerStatus {
        return RosterPlayerStatus(
            Player(
                id = id,
                teamId = teamId,
                name = "player${id.toInt()}",
                shirtNumber = id.toInt(),
                licenseNumber = id,
                image = null,
                positions = positions
            ), status, playerNumberOverlay
        )
    }

    fun generateFullLineup(
        lineup: Lineup,
        strategy: TeamStrategy,
        withDpDh: Boolean
    ): List<PlayerWithPosition> {
        val players: MutableList<PlayerWithPosition> = mutableListOf()
        var index = 1
        var order = 1
        strategy.positions.forEach {
            if (it.isDefensePlayer()) {
                players.add(generate(index.toLong(), it, 0, order))
                order++
            }
            index++
        }
        if (withDpDh) {
            var oldOrder = 0
            players.first { it.isPitcher() }.apply {
                oldOrder = this.order
                this.flags = PlayerFieldPosition.FLAG_FLEX
                this.order = strategy.getDesignatedPlayerOrder(lineup.extraHitters)
            }
            players.add(generate(42L, FieldPosition.DP_DH, PlayerFieldPosition.FLAG_NONE, oldOrder))
        }
        return players
    }
}
