/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.graphics.Bitmap
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.telen.easylineup.R
import com.telen.easylineup.compose.HeaderIconChip
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.lineup.attack.AttackScreen
import com.telen.easylineup.lineup.defense.DefenseScreen
import com.telen.easylineup.views.LineupTypeface
import kotlinx.coroutines.launch

/**
 * The Lineup container - app bar, Defense/Offense tabbar and (editable only) Save/Cancel
 * formbar, matching the mockup's chrome around the Defense/Attack screens, which otherwise keep
 * their own look untouched. Replaces the legacy `TabLayout`/`ViewPager2`/`BottomChoiceView` XML
 * container and the two child Fragments (`DefenseFragment`/`AttackFragment`) it used to host -
 * both screens are now plain composables sharing this one Fragment's `LineupViewModel` directly,
 * with no parent/child Fragment relationship left to maintain.
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
    onShareCaptured: (Bitmap) -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    defenseStrategy: TeamStrategy,
    defensePlayers: List<PlayerWithPosition>,
    lineupMode: Int,
    teamType: Int,
    extraHitters: Int,
    onPositionClicked: (FieldPosition) -> Unit,
    onPlayersSwitched: (PlayerWithPosition, PlayerWithPosition) -> Unit,
    onPlayerReassigned: (PlayerWithPosition, FieldPosition) -> Unit,
    onPlayerSentToTrash: (Player, FieldPosition) -> Unit,
    onSubstituteLongClicked: (Player) -> Unit,
    onAddSubstitute: () -> Unit,
    onLineupModeChanged: (Boolean) -> Unit,
    batters: List<BatterState>,
    typeface: LineupTypeface,
    onReorder: (List<BatterState>) -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

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
                if (selectedTab == FRAGMENT_DEFENSE_INDEX) {
                    R.string.new_lineup_tab_field_defense
                } else {
                    R.string.new_lineup_tab_field_offense
                }
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
            actions = {
                if (!editable) {
                    HeaderIconChip(
                        icon = R.drawable.ic_share_outline_24dp,
                        contentDescription = stringResource(R.string.action_share),
                        onClick = {
                            scope.launch {
                                onShareCaptured(graphicsLayer.toImageBitmap().asAndroidBitmap())
                            }
                        }
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
        Box(
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            if (selectedTab == FRAGMENT_DEFENSE_INDEX) {
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
                    typeface = typeface,
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
            active = selectedTab == FRAGMENT_DEFENSE_INDEX,
            onClick = { onTabSelected(FRAGMENT_DEFENSE_INDEX) },
            modifier = Modifier.weight(1f)
        )
        LineupTabItem(
            label = stringResource(R.string.new_lineup_tab_field_offense),
            active = selectedTab == FRAGMENT_ATTACK_INDEX,
            onClick = { onTabSelected(FRAGMENT_ATTACK_INDEX) },
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    fontWeight = FontWeight.Bold
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
