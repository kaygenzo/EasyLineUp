/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class GetOnlyPlayersInField(private val dispatcherProvider: DispatcherProvider) {
    suspend operator fun invoke(
        playersInLineup: List<PlayerWithPosition>
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            playersInLineup.filter { it.position > 0 && !it.isSubstitute() && !it.isDpDh() }
        }
    }
}
