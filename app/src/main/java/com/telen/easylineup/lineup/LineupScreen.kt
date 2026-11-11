/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderIconChip
import com.telen.easylineup.compose.NARROW_BUTTON_PADDING
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.attack.AttackScreen
import com.telen.easylineup.lineup.defense.DefenseScreen

/**
 * The Lineup container - app bar, Defense/Offense tabbar and (editable only) Save/Cancel
 * formbar, matching the mockup's chrome around the Defense/Attack screens, which otherwise keep
 * their own look untouched. Both are plain composables sharing the one `LineupViewModel`.
 */
@Composable
fun LineupScreen(
    lineupName: String,
    editable: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareLineup: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onEditRosterClick: () -> Unit,
    defenseStrategy: TeamStrategy,
    defensePlayers: List<PlayerWithPosition>,
    lineupMode: Int,
    teamType: TeamType,
    extraHitters: Int,
    onPositionClicked: (FieldPosition) -> Unit,
    onPlayersSwitched: (PlayerWithPosition, PlayerWithPosition) -> Unit,
    onPlayerReassigned: (PlayerWithPosition, FieldPosition) -> Unit,
    onPlayerSentToTrash: (Player, FieldPosition) -> Unit,
    onSubstituteLongClicked: (Player) -> Unit,
    onAddSubstitute: () -> Unit,
    onLineupModeChanged: (Boolean) -> Unit,
    batters: List<BatterState>,
    onReorder: (List<BatterState>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            icon = if (editable) R.drawable.ic_close_24dp else R.drawable.ic_arrow_left_white_24dp,
            onIconClick = if (editable) onCancelClick else onBackClick,
            iconContentDescription = stringResource(
                if (editable) R.string.cancel_button else R.string.generic_back
            ),
            eyebrow = lineupName,
            title = stringResource(
                if (selectedTab == LINEUP_DEFENSE_TAB) {
                    R.string.new_lineup_tab_field_defense
                } else {
                    R.string.new_lineup_tab_field_offense
                }
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
            actions = {
                if (editable) {
                    HeaderIconChip(
                        icon = R.drawable.ic_people_outline_24dp,
                        contentDescription = stringResource(R.string.action_edit_roster),
                        onClick = onEditRosterClick
                    )
                } else {
                    HeaderIconChip(
                        icon = R.drawable.ic_share_outline_24dp,
                        contentDescription = stringResource(R.string.action_share),
                        onClick = onShareLineup
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderIconChip(
                        icon = R.drawable.ic_delete_outline_24dp,
                        contentDescription = stringResource(R.string.action_delete),
                        onClick = onDeleteClick
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    EditModePill(onClick = onEditClick)
                }
            }
        )
        LineupTabBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == LINEUP_DEFENSE_TAB) {
                DefenseScreen(
                    strategy = defenseStrategy,
                    players = defensePlayers,
                    lineupMode = lineupMode,
                    teamType = teamType,
                    extraHitters = extraHitters,
                    editable = editable,
                    onPositionClicked = onPositionClicked,
                    onPlayersSwitched = onPlayersSwitched,
                    onPlayerReassigned = onPlayerReassigned,
                    onPlayerSentToTrash = onPlayerSentToTrash,
                    onSubstituteLongClicked = onSubstituteLongClicked,
                    onAddSubstitute = onAddSubstitute,
                    onLineupModeChanged = onLineupModeChanged
                )
            } else {
                AttackScreen(
                    batters = batters,
                    editable = editable,
                    onReorder = onReorder
                )
            }
        }
        if (editable) {
            LineupFormBar(onCancelClick = onCancelClick, onSaveClick = onSaveClick)
        }
    }
}

@Composable
private fun EditModePill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = stringResource(R.string.action_edit),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "›",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun LineupTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LineupTabItem(
            label = stringResource(R.string.new_lineup_tab_field_defense),
            active = selectedTab == LINEUP_DEFENSE_TAB,
            onClick = { onTabSelected(LINEUP_DEFENSE_TAB) },
            modifier = Modifier.weight(1f)
        )
        LineupTabItem(
            label = stringResource(R.string.new_lineup_tab_field_offense),
            active = selectedTab == LINEUP_ATTACK_TAB,
            onClick = { onTabSelected(LINEUP_ATTACK_TAB) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LineupTabItem(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (active) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = if (active) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.04.em
        )
    }
}

@Composable
private fun LineupFormBar(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = NARROW_BUTTON_PADDING
            ) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_save_lineup),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val LineupScreenPreviewDefensePlayers = listOf(
    PlayerWithPosition(
        playerName = "Mia Chen",
        playerSex = 0,
        shirtNumber = 7,
        licenseNumber = "100",
        teamId = 1L,
        image = null,
        position = FieldPosition.PITCHER.id,
        order = 1,
        fieldPositionId = 1L,
        playerId = 1L,
        lineupId = 1L,
        playerPositions = FieldPosition.PITCHER.mask
    ),
    PlayerWithPosition(
        playerName = "Jordan Lee",
        playerSex = 1,
        shirtNumber = 12,
        licenseNumber = "101",
        teamId = 1L,
        image = null,
        position = FieldPosition.SHORT_STOP.id,
        order = 2,
        fieldPositionId = 2L,
        playerId = 2L,
        lineupId = 1L,
        playerPositions = FieldPosition.SHORT_STOP.mask
    )
)

private val LineupScreenPreviewBatters = listOf(
    BatterState(
        playerId = 1L,
        playerFlag = 0,
        playerOrder = 1,
        playerName = "Mia Chen",
        playerNumber = "7",
        playerPosition = FieldPosition.PITCHER,
        playerPositionDesc = "Pitcher",
        canShowPosition = true,
        canMove = true,
        canShowDescription = true,
        canShowOrder = true,
        applyBackground = false,
        isEditable = true
    ),
    BatterState(
        playerId = 2L,
        playerFlag = 0,
        playerOrder = 2,
        playerName = "Jordan Lee",
        playerNumber = "12",
        playerPosition = FieldPosition.SHORT_STOP,
        playerPositionDesc = "Shortstop",
        canShowPosition = true,
        canMove = true,
        canShowDescription = true,
        canShowOrder = true,
        applyBackground = false,
        isEditable = true
    )
)

@DugoutPreviews
@Composable
private fun LineupScreenPreview() {
    DugoutTheme {
        LineupScreen(
            lineupName = "vs Riverside Hawks",
            editable = true,
            selectedTab = LINEUP_DEFENSE_TAB,
            onTabSelected = {},
            onBackClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onShareLineup = {},
            onDeleteClick = {},
            onEditClick = {},
            onEditRosterClick = {},
            defenseStrategy = TeamStrategy.STANDARD,
            defensePlayers = LineupScreenPreviewDefensePlayers,
            lineupMode = MODE_ENABLED,
            teamType = TeamType.BASEBALL,
            extraHitters = 1,
            onPositionClicked = {},
            onPlayersSwitched = { _, _ -> },
            onPlayerReassigned = { _, _ -> },
            onPlayerSentToTrash = { _, _ -> },
            onSubstituteLongClicked = {},
            onAddSubstitute = {},
            onLineupModeChanged = {},
            batters = LineupScreenPreviewBatters,
            onReorder = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun EditModePillPreview() {
    DugoutTheme {
        EditModePill(onClick = {})
    }
}

@DugoutPreviews
@Composable
private fun LineupTabBarPreview() {
    DugoutTheme {
        LineupTabBar(
            selectedTab = LINEUP_DEFENSE_TAB,
            onTabSelected = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupTabItemPreview() {
    DugoutTheme {
        LineupTabItem(
            label = "Defense",
            active = true,
            onClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupFormBarPreview() {
    DugoutTheme {
        LineupFormBar(onCancelClick = {}, onSaveClick = {})
    }
}
