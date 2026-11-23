/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.LoadFailedState
import com.telen.easylineup.compose.isWide
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.player.PlayerDeleteFlow
import com.telen.easylineup.player.PlayerProfilePage
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.startActivityOrNotify
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel

/** The team roster, the `navigation_team` root tab. */
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

    // Wide, the roster is a list-detail: tapping a player shows their profile in the other
    // pane instead of opening it as a screen of its own, so a coach can walk down the roster.
    var selectedPlayerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingDeletePlayerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDeleteSheet by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = isWide
        val shownPlayerId = selectedPlayerId?.takeIf { id -> players.any { it.id == id } }
            ?: players.firstOrNull()?.id
        val roster: @Composable () -> Unit = {
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
                    if (wide) {
                        selectedPlayerId = playerId
                    } else {
                        navController.navigate(Routes.PlayerDetails(playerId))
                    }
                },
                onAddPlayerClick = {
                    FirebaseAnalyticsUtils.onClick(context, "click_team_players_create")
                    navController.navigate(Routes.PlayerEdit())
                },
                onMessageTeamClick = {
                    FirebaseAnalyticsUtils.onClick(context, "click_team_players_message")
                    showMessageSheet = true
                },
                selectedPlayerId = if (wide) shownPlayerId else null
            )
        }
        if (wide && shownPlayerId != null) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { roster() }
                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Box(modifier = Modifier.weight(1.1f)) {
                    key(shownPlayerId) {
                        PlayerProfilePage(
                            playerId = shownPlayerId,
                            onBackClick = null,
                            onEditRequested = { playerId ->
                                FirebaseAnalyticsUtils.onClick(context, "click_player_details_edit")
                                navController.navigate(Routes.PlayerEdit(playerId))
                            },
                            onDeleteRequested = { playerId ->
                                pendingDeletePlayerId = playerId
                                showDeleteSheet = true
                            }
                        )
                    }
                }
            }
        } else {
            roster()
        }
    }

    pendingDeletePlayerId?.let { playerId ->
        PlayerDeleteFlow(
            playerId = playerId,
            showSheet = showDeleteSheet,
            onPendingChanged = { pendingDeletePlayerId = it },
            onShowSheetChanged = { showDeleteSheet = it },
            onDeleted = { selectedPlayerId = null }
        )
    }

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
                        ("smsto:" + numbers.joinToString(";")).toUri()
                    )
                    context.startActivityOrNotify(intent)
                }
            },
            onEmailClick = {
                FirebaseAnalyticsUtils.onClick(context, "click_team_players_message_email")
                val emails = players.mapNotNull { it.email?.takeIf(String::isNotBlank) }
                if (emails.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
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
    imageUri = image?.toUri(),
    battingSide = PlayerSide.getSideByValue(batting),
    pitchingSide = PlayerSide.getSideByValue(pitching),
    phone = phone,
    email = email
)
