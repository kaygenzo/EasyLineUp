/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.dashboard.DashboardViewModel
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.GeocodingProvider
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.lineup.create.LineupCreationViewModel
import com.telen.easylineup.lineup.edition.LineupEditionViewModel
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.player.PlayerViewModel
import com.telen.easylineup.settings.SettingsViewModel
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.SetupViewModel
import com.telen.easylineup.tournaments.list.LineupViewModel as TournamentLineupViewModel
import com.telen.easylineup.tournaments.statistics.TournamentStatisticsViewModel
import com.telen.easylineup.utils.DispatcherProviderImpl
import com.telen.easylineup.utils.GeocodingProviderImpl
import com.telen.easylineup.utils.PhoneNumberValidatorImpl
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.utils.StringResourcesProviderImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModules = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }
    single<StringResourcesProvider> { StringResourcesProviderImpl(get()) }
    single<GeocodingProvider> { GeocodingProviderImpl(get()) }
    single<PhoneNumberValidator> { PhoneNumberValidatorImpl() }
    single { SharedPreferencesHelper(get()) }

    // Constructor-injected ViewModel — the pattern new/reskinned screens should
    // follow going forward, replacing the KoinComponent field-injection pattern
    // still used by every other ViewModel in this codebase.
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SetupViewModel)
    viewModelOf(::TeamViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::LineupCreationViewModel)
    viewModelOf(::LineupEditionViewModel)
    viewModelOf(::TournamentStatisticsViewModel)

    // Still KoinComponent/by-inject() internally (out of scope to rewrite here) - registered
    // only so koinViewModel() can resolve them now that there is no Fragment to scope them via
    // by viewModels()/activityViewModels(). Two distinct classes share the "LineupViewModel"
    // simple name (com.telen.easylineup.lineup and .tournaments.list) - both need their own
    // registration since Koin resolves by type, not by name.
    viewModelOf(::LineupViewModel)
    viewModelOf(::TournamentLineupViewModel)
    viewModelOf(::LoginViewModel)
}
