/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.GetTeam

/** Whether a main team already exists, to route the splash screen to Home or Login. */
class SplashViewModel(private val getTeamUseCase: GetTeam) : ViewModel() {
    suspend fun getTeam(): Result<Team> = getTeamUseCase()
}
