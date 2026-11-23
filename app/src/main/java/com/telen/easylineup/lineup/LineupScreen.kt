/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.HeaderPill
import com.telen.easylineup.compose.HeaderIconChip
import com.telen.easylineup.compose.ModeBadge
import com.telen.easylineup.compose.NARROW_BUTTON_PADDING
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.compose.buttonLabel
import com.telen.easylineup.compose.headerActionLabel
import com.telen.easylineup.compose.tabLabel
import com.telen.easylineup.compose.uppercaseLocalized
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.attack.AttackScreen
import com.telen.easylineup.lineup.defense.DefenseScreen
import com.telen.easylineup.lineup.defense.DhToggle
import com.telen.easylineup.lineup.defense.defensePaneWidth

/** Tags for what the tests can't find by text: the screen itself, in either layout. */
internal object LineupTestTags {
    const val SCREEN = "lineup_screen"
}

/**
 * The Lineup container - app bar, Defense/Offense tabbar and (editable only) Save/Cancel
 * formbar, matching the mockup's chrome around the Defense/Attack screens, which otherwise keep
 * their own look untouched. Both are plain composables sharing the one `LineupViewModel`.
 *
 * In a window wider than tall (any landscape phone or tablet, or a wide split screen) the two
 * screens sit side by side, defense on the left and offense on the right, half the width each,
 * as the legacy screen had them: no tabbar then, and the header shows the lineup's name. In
 * edit mode the Save button moves up into the header too (the close icon already cancels), so a
 * 360 dp tall landscape phone doesn't lose another bar's height under the two screens.
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
    val defense: @Composable (Boolean) -> Unit = { sideBySide ->
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
            onLineupModeChanged = onLineupModeChanged,
            sideBySide = sideBySide
        )
    }
    val attack: @Composable (Boolean) -> Unit = { sideBySide ->
        AttackScreen(
            batters = batters,
            editable = editable,
            onReorder = onReorder,
            sideBySide = sideBySide,
            // Side by side, the DP/DH toggle sits above the batting order it changes.
            header = if (editable && sideBySide) {
                {
                    DhToggle(
                        strategy = defenseStrategy,
                        players = defensePlayers,
                        teamType = teamType,
                        lineupMode = lineupMode,
                        onLineupModeChanged = onLineupModeChanged,
                        compact = true
                    )
                }
            } else {
                null
            }
        )
    }

    LineupLayout(
        lineupName = lineupName,
        editable = editable,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onBackClick = onBackClick,
        onCancelClick = onCancelClick,
        onSaveClick = onSaveClick,
        onShareLineup = onShareLineup,
        onDeleteClick = onDeleteClick,
        onEditClick = onEditClick,
        onEditRosterClick = onEditRosterClick,
        defense = defense,
        attack = attack
    )
}

/** [LineupScreen]'s layout: tabs in portrait, [defense] and [attack] side by side otherwise. */
@Composable
private fun LineupLayout(
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
    defense: @Composable (sideBySide: Boolean) -> Unit,
    attack: @Composable (sideBySide: Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(LineupTestTags.SCREEN)
    ) {
        val sideBySide = maxWidth > maxHeight
        Column(modifier = Modifier.fillMaxSize()) {
            LineupHeader(
                lineupName = lineupName,
                title = if (sideBySide) {
                    null
                } else {
                    stringResource(
                        if (selectedTab == LINEUP_DEFENSE_TAB) {
                            R.string.new_lineup_tab_field_defense
                        } else {
                            R.string.new_lineup_tab_field_offense
                        }
                    )
                },
                editable = editable,
                onBackClick = onBackClick,
                onCancelClick = onCancelClick,
                onSaveClick = if (sideBySide) onSaveClick else null,
                onShareLineup = onShareLineup,
                onDeleteClick = onDeleteClick,
                onEditClick = onEditClick,
                onEditRosterClick = onEditRosterClick
            )
            if (sideBySide) {
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    // The defense pane is as wide as its field, which its height caps, so a
                    // wide screen gives the rest to the batters instead of empty bands on both
                    // sides of the square field; never more than half, though.
                    val defenseWidth = minOf(defensePaneWidth(maxHeight), maxWidth / 2)
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.width(defenseWidth).fillMaxHeight()) {
                            defense(true)
                        }
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Box(modifier = Modifier.weight(1f)) { attack(true) }
                    }
                }
            } else {
                LineupTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == LINEUP_DEFENSE_TAB) defense(false) else attack(false)
                }
            }
            if (editable && !sideBySide) {
                LineupFormBar(onCancelClick = onCancelClick, onSaveClick = onSaveClick)
            }
        }
    }
}

/**
 * The app bar. [title] is the shown tab's name, with the lineup's name as the eyebrow above it;
 * null when both screens are shown, and the lineup's name becomes the title itself. With an
 * [onSaveClick] (both shown), edit mode shows a Save button next to the roster icon, and one
 * "Editing" badge before them stands for the two screens' own.
 */
@Composable
private fun LineupHeader(
    lineupName: String,
    title: String?,
    editable: Boolean,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: (() -> Unit)?,
    onShareLineup: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onEditRosterClick: () -> Unit
) {
    ScreenHeader(
        icon = if (editable) R.drawable.ic_close_24dp else R.drawable.ic_arrow_left_white_24dp,
        onIconClick = if (editable) onCancelClick else onBackClick,
        iconContentDescription = stringResource(
            if (editable) R.string.cancel_button else R.string.generic_back
        ),
        eyebrow = if (title == null) null else lineupName,
        title = title ?: lineupName,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
        actions = {
            if (editable) {
                if (onSaveClick != null) {
                    ModeBadge(stringResource(R.string.lineup_editing_badge))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                HeaderIconChip(
                    icon = R.drawable.ic_people_outline_24dp,
                    contentDescription = stringResource(R.string.action_edit_roster),
                    onClick = onEditRosterClick
                )
                if (onSaveClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    HeaderPill(
                        label = stringResource(R.string.action_save_lineup),
                        onClick = onSaveClick
                    )
                }
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
                HeaderPill(
                    label = stringResource(R.string.action_edit),
                    onClick = onEditClick,
                    showChevron = true
                )
            }
        }
    )
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
            text = label.uppercaseLocalized(),
            color = if (active) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.tabLabel
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
                    style = MaterialTheme.typography.buttonLabel,
                    text = stringResource(R.string.cancel_button),
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
                    style = MaterialTheme.typography.buttonLabel,
                    text = stringResource(R.string.action_save_lineup),
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
private fun LineupScreenViewModePreview() {
    DugoutTheme {
        LineupScreen(
            lineupName = "vs Riverside Hawks",
            editable = false,
            selectedTab = LINEUP_ATTACK_TAB,
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
private fun LineupLayoutPreview() {
    DugoutTheme {
        LineupLayout(
            lineupName = "vs Riverside Hawks",
            editable = false,
            selectedTab = LINEUP_DEFENSE_TAB,
            onTabSelected = {},
            onBackClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onShareLineup = {},
            onDeleteClick = {},
            onEditClick = {},
            onEditRosterClick = {},
            defense = { Text("Defense") },
            attack = { Text("Offense") }
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupHeaderPreview() {
    DugoutTheme {
        LineupHeader(
            lineupName = "vs Riverside Hawks",
            title = "Defense",
            editable = false,
            onBackClick = {},
            onCancelClick = {},
            onSaveClick = null,
            onShareLineup = {},
            onDeleteClick = {},
            onEditClick = {},
            onEditRosterClick = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun LineupHeaderSideBySidePreview() {
    DugoutTheme {
        LineupHeader(
            lineupName = "vs Riverside Hawks",
            title = null,
            editable = true,
            onBackClick = {},
            onCancelClick = {},
            onSaveClick = {},
            onShareLineup = {},
            onDeleteClick = {},
            onEditClick = {},
            onEditRosterClick = {}
        )
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
