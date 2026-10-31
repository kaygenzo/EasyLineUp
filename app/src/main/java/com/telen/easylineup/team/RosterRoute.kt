/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/** Replaces `PlayersFragment` - the `navigation_team` root tab. */
@Composable
fun RosterRoute(navController: NavController) {
    val viewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var teamName by remember { mutableStateOf("") }
    var players by remember { mutableStateOf<List<RosterRowState>>(emptyList()) }
    var sortType by remember { mutableStateOf(TeamViewModel.SortType.ALPHA) }
    var showMessageSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.observePlayers()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { playerList -> players = playerList.map { it.toRosterRowState() } }
            .launchIn(this)

        viewModel.observeCurrentTeamName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(this)
    }

    RosterScreen(
        state = RosterState(
            teamName = teamName,
            players = players,
            sortType = sortType
        ),
        onBackClick = { navController.navigateUp() },
        onSortSelected = { newSortType ->
            sortType = newSortType
            viewModel.setSortType(newSortType)
        },
        onPlayerClick = { playerId ->
            FirebaseAnalyticsUtils.onClick(context, "click_team_players_selected")
            navController.navigate(Routes.PlayerDetails(playerId))
        },
        onAddPlayerClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_team_players_create")
            navController.navigate(Routes.PlayerEdit())
        },
        onMessageTeamClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_team_players_message")
            showMessageSheet = true
        }
    )

    if (showMessageSheet) {
        MessageTeamSheet(
            teamName = teamName,
            players = players,
            onDismiss = { showMessageSheet = false },
            onTextMessageClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_text")
                val numbers = players.mapNotNull { it.phone?.takeIf(String::isNotBlank) }
                if (numbers.isNotEmpty()) {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + numbers.joinToString(";"))
                    )
                    launchOrNotify(context, intent)
                }
            },
            onEmailClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_email")
                val emails = players.mapNotNull { it.email?.takeIf(String::isNotBlank) }
                if (emails.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                    }
                    launchOrNotify(context, intent)
                }
            },
            onShareAnotherWayClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_share")
                val text = players.joinToString("\n") { row ->
                    listOfNotNull(row.name, row.phone, row.email).joinToString(" · ")
                }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                    as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(teamName, text))
                Toast.makeText(context, R.string.roster_message_share_copied, Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }
}

private fun launchOrNotify(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        Toast.makeText(context, R.string.roster_message_no_app, Toast.LENGTH_SHORT).show()
    }
}

private fun Player.toRosterRowState(): RosterRowState = RosterRowState(
    id = id,
    name = name,
    shirtNumber = shirtNumber,
    imageUri = image?.let { Uri.parse(it) },
    battingSide = PlayerSide.getSideByValue(batting),
    pitchingSide = PlayerSide.getSideByValue(pitching),
    phone = phone,
    email = email
)
