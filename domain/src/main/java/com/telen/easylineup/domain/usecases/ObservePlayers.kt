/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository

class ObservePlayers(private val dao: PlayerRepository) {
    fun execute(teamId: Long): LiveData<List<Player>> = dao.observePlayers(teamId)
}
