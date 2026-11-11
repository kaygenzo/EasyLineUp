/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.LoadFailedState
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.startActivityOrNotify
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel

/** Replaces `PlayersFragment` - the `navigation_team` root tab. */
@Composable
fun RosterRoute(navController: NavController) {
    val viewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val players = remember(uiState.players) {
        uiState.players.map { it.toRosterRowState() }.toImmutableList()
    }

    var showMessageSheet by rememberSaveable { mutableStateOf(false) }

    if (uiState.loadFailed) {
        LoadFailedState(onRetry = viewModel::onRetryLoad)
        return
    }

    RosterScreen(
        state = RosterState(
            teamName = uiState.teamName,
            players = players,
            sortType = uiState.sortType
        ),
        onBackClick = { navController.navigateUp() },
        onSortSelected = viewModel::setSortType,
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
            teamName = uiState.teamName,
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
                    context.startActivityOrNotify(intent)
                }
            },
            onEmailClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_email")
                val emails = players.mapNotNull { it.email?.takeIf(String::isNotBlank) }
                if (emails.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                        putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
                    }
                    context.startActivityOrNotify(intent)
                }
            },
            onShareAnotherWayClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_share")
                val text = players.joinToString("\n") { row ->
                    listOfNotNull(row.name, row.phone, row.email).joinToString(" · ")
                }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                    as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(uiState.teamName, text))
                Toast.makeText(context, R.string.roster_message_share_copied, Toast.LENGTH_SHORT)
                    .show()
            }
        )
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
