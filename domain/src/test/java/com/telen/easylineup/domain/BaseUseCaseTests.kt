package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition

open class BaseUseCaseTests {
    protected fun generatePlayerWithPosition(id: Long, teamID: Long, position: FieldPosition, flag: Int, order: Int, lineupID: Long): PlayerWithPosition {
        return PlayerWithPosition(playerName = "player${id.toInt()}",
                shirtNumber = id.toInt(),
                licenseNumber = id,
                teamId = teamID,
                image = null,
                position = position.position,
                x = 0f, y = 0f,
                flags = flag,
                order = order,
                fieldPositionID = id,
                playerID = id,
                lineupId = lineupID,
                playerPositions = 1)
    }
}