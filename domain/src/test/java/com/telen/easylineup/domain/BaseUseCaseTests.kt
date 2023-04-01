package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*

open class BaseUseCaseTests {
    private fun generatePlayerWithPosition(
        id: Long,
        teamID: Long,
        position: FieldPosition?,
        flag: Int,
        order: Int,
        lineupID: Long,
        positions: Int = 1
    ): PlayerWithPosition {
        return PlayerWithPosition(
            playerName = "player${id.toInt()}",
            shirtNumber = id.toInt(),
            licenseNumber = id,
            teamId = teamID,
            image = null,
            position = position?.id ?: -1,
            x = 0f, y = 0f,
            flags = flag,
            order = order,
            fieldPositionID = id,
            playerID = id,
            lineupId = lineupID,
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
        val players = mutableListOf<PlayerWithPosition>()
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