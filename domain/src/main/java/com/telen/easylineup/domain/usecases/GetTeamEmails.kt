/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class GetTeamEmails(
    private val getPlayers: GetPlayers,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(): Result<List<String>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            getPlayers().getOrThrow()
                .filter { !it.email.isNullOrEmpty() }
                .map { it.email ?: "" }
        }
    }
}
