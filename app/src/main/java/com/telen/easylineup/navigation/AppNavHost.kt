/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.telen.easylineup.HomeViewModel
import com.telen.easylineup.dashboard.DashboardRoute
import com.telen.easylineup.lineup.LineupRoute
import com.telen.easylineup.lineup.create.LineupCreationRoute
import com.telen.easylineup.player.PlayerDetailsRoute
import com.telen.easylineup.player.PlayerEditRoute
import com.telen.easylineup.settings.SettingsRoute
import com.telen.easylineup.team.RosterRoute
import com.telen.easylineup.team.details.TeamDetailsRoute
import com.telen.easylineup.tournaments.list.TournamentListRoute
import com.telen.easylineup.tournaments.statistics.TournamentStatisticsRoute
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

private const val SLIDE_DURATION_MILLIS = 300

/**
 * The app's navigation graph: every destination below is a `composable<Routes.X>` with
 * type-safe route arguments. The 4 slide transitions are set once here rather than at every
 * `navigate()` call site.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        val entry = currentBackStackEntry ?: return@LaunchedEffect
        // The old Fragment class names, on purpose: the analytics dashboards and their history
        // are keyed by them, and renaming would split each screen's numbers in two.
        val screenName = when {
            entry.destination.hasRoute<Routes.Dashboard>() -> "DashboardFragment"
            entry.destination.hasRoute<Routes.Roster>() -> "TeamFragment"
            entry.destination.hasRoute<Routes.Tournaments>() -> "TournamentListFragment"
            entry.destination.hasRoute<Routes.Settings>() -> "Settings"
            entry.destination.hasRoute<Routes.PlayerDetails>() -> "PlayerDetailsFragment"
            entry.destination.hasRoute<Routes.PlayerEdit>() -> "PlayerEditFragment"
            entry.destination.hasRoute<Routes.LineupCreation>() -> "LineupCreationFragment"
            entry.destination.hasRoute<Routes.TeamDetails>() -> "TeamDetailsFragment"
            entry.destination.hasRoute<Routes.TournamentStatistics>() ->
                "TournamentStatisticsFragment"
            entry.destination.hasRoute<Routes.Lineup>() -> {
                if (entry.toRoute<Routes.Lineup>().editable) {
                    "LineupFragmentEditable"
                } else {
                    "LineupFragmentFixed"
                }
            }
            else -> null
        }
        screenName?.let { FirebaseAnalyticsUtils.onScreen(context, it) }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(SLIDE_DURATION_MILLIS)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(SLIDE_DURATION_MILLIS)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(SLIDE_DURATION_MILLIS)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(SLIDE_DURATION_MILLIS)
            )
        }
    ) {
        composable<Routes.Dashboard> { DashboardRoute(navController, homeViewModel) }
        composable<Routes.Roster> { RosterRoute(navController) }
        composable<Routes.Tournaments> { TournamentListRoute(navController) }
        composable<Routes.Settings> { SettingsRoute(navController) }
        composable<Routes.PlayerDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.PlayerDetails>()
            PlayerDetailsRoute(navController, route.playerId)
        }
        composable<Routes.PlayerEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.PlayerEdit>()
            PlayerEditRoute(navController, route.playerId)
        }
        composable<Routes.LineupCreation> { LineupCreationRoute(navController) }
        composable<Routes.TeamDetails> { TeamDetailsRoute(navController) }
        composable<Routes.TournamentStatistics> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.TournamentStatistics>()
            TournamentStatisticsRoute(
                navController = navController,
                tournamentId = route.tournamentId,
                tournamentName = route.tournamentName,
                teamTypeId = route.teamType
            )
        }
        composable<Routes.Lineup> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.Lineup>()
            LineupRoute(
                navController = navController,
                lineupId = route.lineupId,
                editable = route.editable,
                fromShortcut = route.fromShortcut
            )
        }
    }
}
