/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.SubstituteBattingSlots
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.matches
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.usecases.GetSubstituteBattingSlots
import com.telen.easylineup.utils.StringResourcesProviderImpl
import com.telen.easylineup.views.StateDefense

/**
 * Replaces the legacy `ListAvailablePlayersBottomSheet`/`PlayerListView` - same filter/badge
 * logic (a player's own eligible positions highlighted against [filterPosition]), as a Compose
 * `ModalBottomSheet`. Picking a substitute (`filterPosition == FieldPosition.SUBSTITUTE`) gets
 * the mockup's extra-hitter budget framing: a subtitle stating how many slots are free, a
 * "Will bat"/"Bench only" pill, and a footnote - otherwise this is a plain roster picker.
 * The pill is the same for every row: `AssignPlayerFieldPosition` decides purely from how many
 * slots are free at the moment of picking, not from which candidate gets picked - whichever one
 * the user taps first gets the free slot, so there is no meaningful per-row difference to show.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailablePlayersSheet(
    players: List<PlayerWithPosition>,
    lineupPlayers: List<PlayerWithPosition>,
    filterPosition: FieldPosition?,
    teamType: Int,
    extraHitters: Int,
    onPlayerSelected: (Player) -> Unit,
    onDismiss: () -> Unit,
    getSubstituteBattingSlots: GetSubstituteBattingSlots = GetSubstituteBattingSlots()
) {
    val context = LocalContext.current
    val positionShortNames = StringResourcesProviderImpl(context).positionShortNames(teamType)
    val isSubstituteSheet = filterPosition == FieldPosition.SUBSTITUTE

    // The budget must be counted from the full lineup roster (`lineupPlayers`), not from
    // `players` - the latter is already the not-yet-assigned candidate list, so none of its
    // entries could ever count as an already-batting substitute.
    val battingSlots = remember(lineupPlayers, extraHitters) {
        getSubstituteBattingSlots(lineupPlayers, extraHitters)
    }
    val nextPickWillBat = when (battingSlots) {
        SubstituteBattingSlots.Unlimited -> true
        is SubstituteBattingSlots.Limited -> battingSlots.remaining > 0
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = stringResource(
                        if (isSubstituteSheet) {
                            R.string.defense_add_substitute_sheet_title
                        } else {
                            R.string.defense_select_player_sheet_title
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isSubstituteSheet) {
                    Text(
                        text = when (battingSlots) {
                            SubstituteBattingSlots.Unlimited ->
                                stringResource(R.string.defense_add_substitute_budget_unlimited)

                            is SubstituteBattingSlots.Limited -> if (battingSlots.remaining > 0) {
                                stringResource(
                                    R.string.defense_add_substitute_budget_free,
                                    battingSlots.remaining,
                                    battingSlots.total
                                )
                            } else {
                                stringResource(R.string.defense_add_substitute_budget_none)
                            }
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(players, key = { entry -> entry.playerId }) { entry ->
                    val player = entry.toPlayer()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayerSelected(player) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DefenseMarker(
                            state = StateDefense.PLAYER,
                            imageUri = player.image?.let(Uri::parse),
                            fallbackName = player.name,
                            size = 40.dp,
                            borderColor = Color.Black,
                            label = null,
                            onClick = { onPlayerSelected(player) },
                            enableClick = false
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isSubstituteSheet) {
                                val positionLabel = primaryPositionShortName(
                                    player,
                                    positionShortNames
                                )
                                val positionFormatRes =
                                    R.string.defense_add_substitute_candidate_position_format
                                val benchLabelRes = R.string.defense_add_substitute_bench_label
                                Text(
                                    text = positionLabel
                                        ?.let { stringResource(positionFormatRes, it) }
                                        ?: stringResource(benchLabelRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isSubstituteSheet) {
                            CandidatePill(
                                text = stringResource(
                                    if (nextPickWillBat) {
                                        R.string.defense_add_substitute_will_bat
                                    } else {
                                        R.string.defense_add_substitute_bench_only
                                    }
                                ),
                                emphasized = nextPickWillBat
                            )
                        } else {
                            val matchingPosition = filterPosition
                                ?.takeIf { it.matches(player.positions) }
                            if (matchingPosition != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = positionShortNames[matchingPosition.ordinal],
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
            if (isSubstituteSheet) {
                Text(
                    text = stringResource(R.string.defense_add_substitute_footnote),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun CandidatePill(text: String, emphasized: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (emphasized) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun primaryPositionShortName(player: Player, shortNames: Array<String>): String? {
    return FieldPosition.findPrimaryDefensePosition(player.positions)
        ?.let { shortNames.getOrNull(it.ordinal) }
}

private val AvailablePlayersSheetPreviewCandidates = listOf(
    PlayerWithPosition(
        playerName = "Mia Chen",
        playerSex = 0,
        shirtNumber = 7,
        licenseNumber = "100",
        teamId = 1L,
        image = null,
        position = -1,
        playerId = 1L,
        lineupId = 1L,
        playerPositions = FieldPosition.PITCHER.mask or FieldPosition.CATCHER.mask
    ),
    PlayerWithPosition(
        playerName = "Jordan Lee",
        playerSex = 1,
        shirtNumber = 12,
        licenseNumber = "101",
        teamId = 1L,
        image = null,
        position = -1,
        playerId = 2L,
        lineupId = 1L,
        playerPositions = FieldPosition.SHORT_STOP.mask
    ),
    PlayerWithPosition(
        playerName = "Ava Brooks",
        playerSex = 1,
        shirtNumber = 23,
        licenseNumber = "102",
        teamId = 1L,
        image = null,
        position = -1,
        playerId = 3L,
        lineupId = 1L,
        playerPositions = 0
    )
)

@DugoutPreviews
@Composable
private fun AvailablePlayersSheetPreview() {
    DugoutTheme {
        AvailablePlayersSheet(
            players = AvailablePlayersSheetPreviewCandidates,
            lineupPlayers = emptyList(),
            filterPosition = FieldPosition.SUBSTITUTE,
            teamType = TeamType.BASEBALL.id,
            extraHitters = 2,
            onPlayerSelected = {},
            onDismiss = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun CandidatePillPreview() {
    DugoutTheme {
        CandidatePill(text = "Will bat", emphasized = true)
    }
}
