/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository

class ObservePlayer(private val dao: PlayerRepository) {
    operator fun invoke(playerId: Long): LiveData<Player> = dao.getPlayerById(playerId)
}
