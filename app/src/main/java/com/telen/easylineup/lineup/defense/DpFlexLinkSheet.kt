/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.NARROW_BUTTON_PADDING
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.TeamType
import kotlinx.coroutines.launch

private enum class DpFlexSlot { DP, FLEX }
private val SLOT_AVATAR_SIZE = 44.dp

/**
 * Replaces the legacy `DpFlexLinkView` dialog - a bottom sheet (mockup's `.sheet` +
 * `.dp-flex-slot` rows + bottom `.formbar`) instead of a native `AlertDialog`. Tapping an
 * unlocked slot swaps the sheet's content to a roster picker, same "content swap inside the
 * same sheet, never a second one stacked on top" pattern used everywhere else in the mockup -
 * the drag handle is replaced by a back row while browsing, exactly like the mockup's "Edit
 * lineup / roster step" screen does for its own sub-picker.
 * Non-dismissible by swipe/scrim-tap/back, matching the legacy `setCancelable(false)` - the
 * business rule requires assigning both slots or explicitly cancelling via the formbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpFlexLinkSheet(
    initialDp: Player?,
    initialFlex: Player?,
    dpBattingOrder: Int?,
    dpLocked: Boolean,
    flexLocked: Boolean,
    teamType: Int,
    title: String,
    onRequestDpPlayers: suspend () -> List<Player>,
    onRequestFlexPlayers: suspend () -> List<Player>,
    onConfirm: (dp: Player?, flex: Player?) -> Unit,
    onDismiss: () -> Unit
) {
    var dp by remember { mutableStateOf(initialDp) }
    var flex by remember { mutableStateOf(initialFlex) }
    var selecting by remember { mutableStateOf<DpFlexSlot?>(null) }
    var selectablePlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    val scope = rememberCoroutineScope()
    // Block only the transition into Hidden (swipe/scrim/back dismiss) - blocking every
    // transition would also block the initial Hidden -> Expanded open animation.
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { it != SheetValue.Hidden }
    )

    val dpLabel = stringResource(
        if (teamType == TeamType.SOFTBALL.id) {
            R.string.field_position_dp
        } else {
            R.string.field_position_dh
        }
    )
    val flexLabel = stringResource(
        if (teamType == TeamType.SOFTBALL.id) {
            R.string.field_position_flex
        } else {
            R.string.field_position_pitcher
        }
    )
    // The batting order number is only reliable for the pair the sheet opened with - picking a
    // new DP changes which slot they'll actually land on, which isn't known until confirmed.
    val dpRole = if (dp?.id == initialDp?.id && dpBattingOrder != null && dpBattingOrder > 0) {
        stringResource(R.string.link_dp_flex_batting_role_format, dpLabel, dpBattingOrder)
    } else {
        dpLabel
    }
    // Unlike the batting number, "won't bat" is a fact about the Flex role itself, true for
    // whichever player holds it - safe to show regardless of whether the pick just changed.
    val flexRole = if (flex != null) {
        stringResource(R.string.link_dp_flex_wont_bat_role_format, flexLabel)
    } else {
        flexLabel
    }

    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
        dragHandle = if (selecting == null) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            if (selecting != null) {
                SheetBackRow(onBack = { selecting = null })
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (selecting == null) {
                DpFlexSlotRow(role = dpRole, player = dp, locked = dpLocked, onClick = {
                    scope.launch {
                        selectablePlayers = onRequestDpPlayers()
                        selecting = DpFlexSlot.DP
                    }
                })
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                DpFlexSlotRow(role = flexRole, player = flex, locked = flexLocked, onClick = {
                    scope.launch {
                        selectablePlayers = onRequestFlexPlayers()
                        selecting = DpFlexSlot.FLEX
                    }
                })
                if (dpLocked || flexLocked) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = stringResource(R.string.link_dp_flex_locked_footnote),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            } else if (selectablePlayers.isEmpty()) {
                Text(
                    text = stringResource(R.string.players_list_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(selectablePlayers, key = { it.id }) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (selecting) {
                                        DpFlexSlot.DP -> dp = player
                                        DpFlexSlot.FLEX -> flex = player
                                        null -> Unit
                                    }
                                    selecting = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DugoutAvatar(
                                name = player.name,
                                imageUri = player.image?.let(Uri::parse),
                                size = SLOT_AVATAR_SIZE
                            )
                            Text(
                                text = player.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 10.dp)
            )
            // Always visible (not just in the two-slot view) - picking a slot must stay
            // escapable even when its roster list turns out empty, same as the legacy
            // dialog's OK/Cancel buttons, which were never conditional on its own
            // in-progress selection state either. 1:2 width ratio matches the mockup's
            // `.formbar__cancel`(flex:1)/`.formbar__save`(flex:2).
            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (selecting != null) selecting = null else onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = NARROW_BUTTON_PADDING
                ) {
                    Text(
                        text = stringResource(android.R.string.cancel),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = { onConfirm(dp, flex) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_confirm),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Replaces the drag handle while the sheet shows the roster sub-picker (mockup's
 * `.sheet__back-row`/`.sheet__back`) - the tell that this is still one sheet mid-transition,
 * not a new one opened on top.
 */
@Composable
private fun SheetBackRow(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 10.dp)
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_down_24dp),
            contentDescription = stringResource(R.string.action_back),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(15.dp)
                .rotate(90f)
        )
    }
}

@Composable
private fun DpFlexSlotRow(
    role: String,
    player: Player?,
    locked: Boolean,
    onClick: () -> Unit
) {
    val gold = if (isSystemInDarkTheme()) Color(0xFFE8B93F) else Color(0xFFB7860B)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.5f else 1f)
            .clickable(enabled = !locked, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (player != null) {
            DugoutAvatar(
                name = player.name,
                imageUri = player.image?.let(Uri::parse),
                size = SLOT_AVATAR_SIZE
            )
        } else {
            val cornerRadius = RoundedCornerShape(SLOT_AVATAR_SIZE * 0.29f)
            Box(
                modifier = Modifier
                    .size(SLOT_AVATAR_SIZE)
                    .clip(cornerRadius)
                    .dashedBorder(
                        MaterialTheme.colorScheme.outline,
                        cornerRadius = SLOT_AVATAR_SIZE * 0.29f
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person_silhouette_24dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SLOT_AVATAR_SIZE * 0.4f)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            // Mockup's `.dp-flex-slot__role` is `text-transform: uppercase; letter-spacing: .06em`.
            Text(
                text = role.uppercase(),
                color = gold,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 0.06.em
            )
            Text(
                text = player?.name ?: stringResource(R.string.defense_dh_slot_unassigned),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (locked) {
            Icon(
                painter = painterResource(R.drawable.ic_lock_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        } else {
            // Mockup's `.select-field__chev` is a plain "›" glyph, not an icon.
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun DpFlexLinkSheetPreview() {
    DugoutTheme {
        DpFlexLinkSheet(
            initialDp = Player(1L, 1L, "Alex Rivera", 12, 0L),
            initialFlex = null,
            dpBattingOrder = 4,
            dpLocked = false,
            flexLocked = false,
            teamType = TeamType.SOFTBALL.id,
            title = "Link DP / Flex",
            onRequestDpPlayers = { listOf(Player(1L, 1L, "Alex Rivera", 12, 0L)) },
            onRequestFlexPlayers = { listOf(Player(2L, 1L, "Sam Cole", 7, 0L)) },
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun SheetBackRowPreview() {
    DugoutTheme {
        SheetBackRow(onBack = {})
    }
}

@DugoutPreviews
@Composable
private fun DpFlexSlotRowPreview() {
    DugoutTheme {
        DpFlexSlotRow(
            role = "Designated Player",
            player = Player(1L, 1L, "Alex Rivera", 12, 0L),
            locked = false,
            onClick = {}
        )
    }
}
