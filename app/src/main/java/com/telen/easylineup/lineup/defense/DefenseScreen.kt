/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.ModeBadge
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.FieldPositionCoordinate
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getPositionPercentage
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.utils.StringResourcesProviderImpl
import com.telen.easylineup.utils.getColor
import com.telen.easylineup.views.StateDefense
import kotlin.math.roundToInt

private const val ICON_SIZE_SCALE = 0.12f
private val TRASH_COORDINATE = FieldPositionCoordinate(100f, 100f)
private val DH_ROW_MIN_HEIGHT = 64.dp

internal data class DefenseMarkerSpec(
    val position: FieldPosition,
    val state: StateDefense,
    val player: PlayerWithPosition?,
    val borderColor: Color,
    val label: String?
)

/**
 * The Defense (field diagram) screen - single composable for both fixed/view and editable
 * modes, matching [com.telen.easylineup.lineup.attack.AttackScreen]'s "one screen, branch on
 * `editable`" shape. Unlike the rest of the Dugout Card redesign, this screen deliberately keeps
 * the legacy screen's own visual language (vector field, circular player markers) instead of the
 * mockup's flat redesign - this was an explicit ask, not an oversight.
 */
@Composable
fun DefenseScreen(
    strategy: TeamStrategy,
    players: List<PlayerWithPosition>,
    lineupMode: Int,
    teamType: Int,
    extraHitters: Int,
    editable: Boolean,
    onPositionClicked: (FieldPosition) -> Unit,
    onPlayersSwitched: (PlayerWithPosition, PlayerWithPosition) -> Unit,
    onPlayerReassigned: (PlayerWithPosition, FieldPosition) -> Unit,
    onPlayerSentToTrash: (Player, FieldPosition) -> Unit,
    onSubstituteLongClicked: (Player) -> Unit,
    onAddSubstitute: () -> Unit,
    onLineupModeChanged: (Boolean) -> Unit
) {
    var showDisableDhConfirm by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = 16.dp
        val fieldSize = maxWidth - horizontalPadding * 2
        val iconSize = fieldSize * ICON_SIZE_SCALE

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 16.dp)
        ) {
            if (editable) {
                ModeBadge(stringResource(R.string.defense_mode_badge_editing))
                Spacer(modifier = Modifier.height(12.dp))
                DhToggleRow(
                    teamType = teamType,
                    checked = lineupMode == MODE_ENABLED,
                    onCheckedChange = { checked ->
                        if (checked) onLineupModeChanged(true) else showDisableDhConfirm = true
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            DugoutCard {
                DefenseField(
                    strategy = strategy,
                    players = players,
                    lineupMode = lineupMode,
                    teamType = teamType,
                    editable = editable,
                    fieldSize = fieldSize,
                    iconSize = iconSize,
                    onPositionClicked = onPositionClicked,
                    onPlayersSwitched = onPlayersSwitched,
                    onPlayerReassigned = onPlayerReassigned,
                    onPlayerSentToTrash = onPlayerSentToTrash
                )
            }

            if (editable && lineupMode == MODE_ENABLED) {
                Spacer(modifier = Modifier.height(12.dp))
                DpDhSlotRow(
                    players = players,
                    teamType = teamType,
                    onClick = { onPositionClicked(FieldPosition.DP_DH) }
                )
            }

            if (editable) {
                Spacer(modifier = Modifier.height(16.dp))
                SubstituteAddRow(
                    extraHitters = extraHitters,
                    players = players,
                    onClick = onAddSubstitute
                )
                val substitutes = players.filter { it.isSubstitute() }
                    // Batting order first (so the row reads left-to-right in the order they'd
                    // bat), falling back to alphabetical for substitutes sharing the same
                    // order - bench-only ones all share SUBSTITUTE_ORDER_VALUE.
                    .sortedWith(compareBy({ it.order }, { it.playerName.lowercase() }))
                if (substitutes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    SubstitutesRow(
                        substitutes = substitutes,
                        iconSize = iconSize,
                        onSubstituteLongClicked = onSubstituteLongClicked
                    )
                }
            }
        }
    }

    if (showDisableDhConfirm) {
        val dpPlayer = players.firstOrNull { it.isDpDh() }
        val flexPlayer = players.firstOrNull { it.isFlex() }
        val isSoftball = teamType == TeamType.SOFTBALL.id
        ConfirmSheet(
            title = stringResource(
                if (isSoftball) {
                    R.string.defense_turn_off_dh_title_softball
                } else {
                    R.string.defense_turn_off_dh_title_baseball
                }
            ),
            message = stringResource(
                R.string.defense_turn_off_dh_message,
                dpPlayer?.playerName.orEmpty(),
                flexPlayer?.playerName.orEmpty()
            ),
            confirmLabel = stringResource(R.string.defense_turn_off_dh_confirm),
            cancelLabel = stringResource(
                if (isSoftball) R.string.defense_keep_dp_flex_on else R.string.defense_keep_dh_on
            ),
            onDismiss = { showDisableDhConfirm = false },
            onConfirm = {
                showDisableDhConfirm = false
                onLineupModeChanged(false)
            }
        )
    }
}

@Composable
private fun DhToggleRow(teamType: Int, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val isSoftball = teamType == TeamType.SOFTBALL.id
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DH_ROW_MIN_HEIGHT)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (isSoftball) R.string.action_add_dp_flex else R.string.action_add_dh
                ),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    if (isSoftball) {
                        R.string.defense_dh_toggle_sub_softball
                    } else {
                        R.string.defense_dh_toggle_sub_baseball
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DpDhSlotRow(
    players: List<PlayerWithPosition>,
    teamType: Int,
    onClick: () -> Unit
) {
    val dpPlayer = players.firstOrNull { it.isDpDh() }
    val flexPlayer = players.firstOrNull { it.isFlex() }
    val isSoftball = teamType == TeamType.SOFTBALL.id
    val badgeLabel = stringResource(
        if (isSoftball) R.string.field_position_dp else R.string.field_position_dh
    )
    val gold = if (isSystemInDarkTheme()) Color(0xFFE8B93F) else Color(0xFFB7860B)
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DH_ROW_MIN_HEIGHT)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, gold, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(gold.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeLabel,
                color = gold,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (dpPlayer != null) {
                Text(
                    text = dpPlayer.playerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val flexPosition = flexPlayer?.let {
                    FieldPosition.getFieldPositionById(it.position)
                }
                val flexPositionShort = flexPosition?.let {
                    StringResourcesProviderImpl(context)
                        .positionShortNames(teamType)
                        .getOrNull(it.ordinal)
                }
                if (flexPlayer != null && flexPositionShort != null) {
                    Text(
                        text = stringResource(
                            R.string.defense_dh_slot_batting_format,
                            flexPlayer.playerName,
                            flexPositionShort,
                            dpPlayer.order
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    text = badgeLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.defense_dh_slot_unassigned),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_down_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .rotate(-90f)
        )
    }
}

@Composable
private fun SubstituteAddRow(
    extraHitters: Int,
    players: List<PlayerWithPosition>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_white_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = stringResource(R.string.defense_add_substitute_content_description),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        val hint = if (extraHitters > Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED) {
            stringResource(R.string.defense_substitute_slots_unlimited)
        } else {
            val batting = players.count {
                it.isSubstitute() && it.order in 1 until Constants.SUBSTITUTE_ORDER_VALUE
            }
            val free = (extraHitters - batting).coerceAtLeast(0)
            stringResource(R.string.defense_substitute_slots_free, free, extraHitters)
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DefenseField(
    strategy: TeamStrategy,
    players: List<PlayerWithPosition>,
    lineupMode: Int,
    teamType: Int,
    editable: Boolean,
    fieldSize: Dp,
    iconSize: Dp,
    onPositionClicked: (FieldPosition) -> Unit,
    onPlayersSwitched: (PlayerWithPosition, PlayerWithPosition) -> Unit,
    onPlayerReassigned: (PlayerWithPosition, FieldPosition) -> Unit,
    onPlayerSentToTrash: (Player, FieldPosition) -> Unit
) {
    val fieldRes = if (strategy == TeamStrategy.B5_DEFAULT) {
        R.drawable.baseball5_field
    } else {
        R.drawable.baseball_field
    }
    val dpDhLabel = stringResource(
        if (teamType == TeamType.SOFTBALL.id) {
            R.string.field_position_dp
        } else {
            R.string.field_position_dh
        }
    )
    val markerSpecs = remember(strategy, players, lineupMode, editable, dpDhLabel) {
        buildMarkerSpecs(strategy, players, lineupMode, editable, dpDhLabel)
    }
    val currentMarkerSpecs by rememberUpdatedState(markerSpecs)

    var draggedPosition by remember { mutableStateOf<FieldPosition?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isOverTrash by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val containerSizePx = with(density) { fieldSize.toPx() }
    val iconSizePx = with(density) { iconSize.toPx() }

    val targets = remember(markerSpecs, containerSizePx, iconSizePx) {
        markerSpecs.map {
            it.position to percentToPx(
                it.position.getPositionPercentage(strategy),
                containerSizePx,
                iconSizePx
            )
        }
    }
    val trashCenter = remember(containerSizePx, iconSizePx) {
        percentToPx(TRASH_COORDINATE, containerSizePx, iconSizePx)
    }

    Box(modifier = Modifier.size(fieldSize)) {
        Image(
            painter = painterResource(fieldRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        markerSpecs.forEach { spec ->
            val center = targets.first { it.first == spec.position }.second
            val isDragged = draggedPosition == spec.position

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (center.x - iconSizePx / 2).roundToInt(),
                            (center.y - iconSizePx / 2).roundToInt()
                        )
                    }
                    .size(iconSize)
                    .zIndex(if (isDragged) 10f else 1f)
                    .graphicsLayer {
                        if (isDragged) {
                            translationX = dragOffset.x
                            translationY = dragOffset.y
                        }
                    }
                    .then(
                        if (editable && spec.state == StateDefense.PLAYER) {
                            Modifier.pointerInput(spec.position, spec.player?.playerId) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedPosition = spec.position
                                        dragOffset = Offset.Zero
                                        isOverTrash = false
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount
                                        val point = center + dragOffset
                                        isOverTrash =
                                            (point - trashCenter).getDistance() < iconSizePx
                                    },
                                    onDragEnd = {
                                        resolveDrop(
                                            source = spec,
                                            dropPoint = center + dragOffset,
                                            targets = targets,
                                            trashCenter = trashCenter,
                                            hitRadius = iconSizePx,
                                            currentSpecs = currentMarkerSpecs,
                                            onPlayersSwitched = onPlayersSwitched,
                                            onPlayerReassigned = onPlayerReassigned,
                                            onPlayerSentToTrash = onPlayerSentToTrash
                                        )
                                        draggedPosition = null
                                        dragOffset = Offset.Zero
                                        isOverTrash = false
                                    },
                                    onDragCancel = {
                                        draggedPosition = null
                                        dragOffset = Offset.Zero
                                        isOverTrash = false
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                DefenseMarker(
                    state = spec.state,
                    imageUri = spec.player?.image?.let(Uri::parse),
                    fallbackName = spec.player?.playerName.orEmpty(),
                    size = iconSize,
                    borderColor = spec.borderColor,
                    label = spec.label,
                    onClick = if (editable) {
                        { onPositionClicked(spec.position) }
                    } else {
                        {}
                    }
                )
                if (spec.state == StateDefense.PLAYER && spec.player != null) {
                    val context = LocalContext.current
                    val dotSize = iconSize / 3
                    SexIndicatorDot(
                        color = Color(Sex.getById(spec.player.playerSex).getColor(context)),
                        dotSize = dotSize,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = dotSize / 2, y = -(dotSize / 2))
                    )
                }
            }
        }

        if (editable) {
            DefenseTrashButton(
                size = iconSize,
                isDragTarget = isOverTrash,
                modifier = Modifier.offset {
                    IntOffset(
                        (trashCenter.x - iconSizePx / 2).roundToInt(),
                        (trashCenter.y - iconSizePx / 2).roundToInt()
                    )
                }
            )
        }
    }
}

@Composable
private fun SubstitutesRow(
    substitutes: List<PlayerWithPosition>,
    iconSize: Dp,
    onSubstituteLongClicked: (Player) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        substitutes.forEach { substitute ->
            Box(
                // combinedClickable's long-press has normal touch-slop tolerance (unlike
                // detectDragGesturesAfterLongPress, which cancels on any finger drift since
                // it's built to recognize a drag starting, not just a hold). DefenseMarker's
                // own clickable is disabled here so it doesn't consume the down event first.
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { onSubstituteLongClicked(substitute.toPlayer()) }
                )
            ) {
                // Same orange/gray coding as the "Will bat"/"Bench only" pill in the add-
                // substitute sheet, but reflecting this substitute's actual current state
                // rather than a prediction.
                val isBatting = substitute.order in 1 until Constants.SUBSTITUTE_ORDER_VALUE
                DefenseMarker(
                    state = StateDefense.PLAYER,
                    imageUri = substitute.image?.let(Uri::parse),
                    fallbackName = substitute.playerName,
                    size = iconSize,
                    borderColor = if (isBatting) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = null,
                    onClick = {},
                    enableClick = false
                )
            }
        }
    }
}

private fun buildMarkerSpecs(
    strategy: TeamStrategy,
    players: List<PlayerWithPosition>,
    lineupMode: Int,
    editable: Boolean,
    dpDhLabel: String
): List<DefenseMarkerSpec> {
    return strategy.positions.map { position ->
        if (position == FieldPosition.DP_DH) {
            val dpPlayer = players.firstOrNull { it.isDpDh() }
            when {
                lineupMode == MODE_DISABLED ->
                    DefenseMarkerSpec(position, StateDefense.NONE, null, Color.Black, null)

                dpPlayer != null ->
                    DefenseMarkerSpec(position, StateDefense.PLAYER, dpPlayer, Color.Red, null)

                editable ->
                    DefenseMarkerSpec(position, StateDefense.DP_DH, null, Color.Black, dpDhLabel)

                else ->
                    DefenseMarkerSpec(position, StateDefense.EMPTY, null, Color.Black, null)
            }
        } else {
            val player = players.firstOrNull {
                it.isDefensePlayer() && FieldPosition.getFieldPositionById(it.position) == position
            }
            when {
                player != null -> {
                    val needsRedBorder = lineupMode == MODE_ENABLED && player.isFlex()
                    DefenseMarkerSpec(
                        position,
                        StateDefense.PLAYER,
                        player,
                        if (needsRedBorder) Color.Red else Color.Black,
                        null
                    )
                }

                editable ->
                    DefenseMarkerSpec(position, StateDefense.ADD_PLAYER, null, Color.Black, null)

                else ->
                    DefenseMarkerSpec(position, StateDefense.EMPTY, null, Color.Black, null)
            }
        }
    }
}

private fun percentToPx(
    coordinate: FieldPositionCoordinate,
    containerSizePx: Float,
    iconSizePx: Float
): Offset {
    var x = coordinate.x * containerSizePx / 100f
    var y = coordinate.y * containerSizePx / 100f

    if (x + iconSizePx / 2 > containerSizePx) x = containerSizePx - iconSizePx / 2
    if (x - iconSizePx / 2 < 0) x = iconSizePx / 2
    if (y - iconSizePx / 2 < 0) y = iconSizePx / 2
    if (y + iconSizePx / 2 > containerSizePx) y = containerSizePx - iconSizePx / 2

    return Offset(x, y)
}

private fun resolveDrop(
    source: DefenseMarkerSpec,
    dropPoint: Offset,
    targets: List<Pair<FieldPosition, Offset>>,
    trashCenter: Offset,
    hitRadius: Float,
    currentSpecs: List<DefenseMarkerSpec>,
    onPlayersSwitched: (PlayerWithPosition, PlayerWithPosition) -> Unit,
    onPlayerReassigned: (PlayerWithPosition, FieldPosition) -> Unit,
    onPlayerSentToTrash: (Player, FieldPosition) -> Unit
) {
    val sourcePlayer = source.player ?: return
    val trashDistance = (dropPoint - trashCenter).getDistance()
    val nearestTarget = targets.minByOrNull { (_, center) ->
        (dropPoint - center).getDistance()
    } ?: return
    val nearestTargetDistance = (dropPoint - nearestTarget.second).getDistance()

    if (trashDistance > hitRadius && nearestTargetDistance > hitRadius) {
        return
    }

    if (trashDistance <= nearestTargetDistance) {
        onPlayerSentToTrash(sourcePlayer.toPlayer(), source.position)
        return
    }

    if (nearestTarget.first == source.position) {
        return
    }

    val targetPlayer = currentSpecs.firstOrNull { it.position == nearestTarget.first }?.player
    if (targetPlayer != null && targetPlayer.playerId != sourcePlayer.playerId) {
        onPlayersSwitched(sourcePlayer, targetPlayer)
    } else {
        onPlayerReassigned(sourcePlayer, nearestTarget.first)
    }
}

@DugoutPreviews
@Composable
private fun DefenseScreenEditablePreview() {
    DugoutTheme {
        DefenseScreen(
            strategy = TeamStrategy.STANDARD,
            players = emptyList(),
            lineupMode = MODE_ENABLED,
            teamType = TeamType.BASEBALL.id,
            extraHitters = 1,
            editable = true,
            onPositionClicked = {},
            onPlayersSwitched = { _, _ -> },
            onPlayerReassigned = { _, _ -> },
            onPlayerSentToTrash = { _, _ -> },
            onSubstituteLongClicked = {},
            onAddSubstitute = {},
            onLineupModeChanged = {}
        )
    }
}

@DugoutPreviews
@Composable
private fun DefenseScreenFixedPreview() {
    DugoutTheme {
        DefenseScreen(
            strategy = TeamStrategy.STANDARD,
            players = emptyList(),
            lineupMode = MODE_ENABLED,
            teamType = TeamType.BASEBALL.id,
            extraHitters = 1,
            editable = false,
            onPositionClicked = {},
            onPlayersSwitched = { _, _ -> },
            onPlayerReassigned = { _, _ -> },
            onPlayerSentToTrash = { _, _ -> },
            onSubstituteLongClicked = {},
            onAddSubstitute = {},
            onLineupModeChanged = {}
        )
    }
}
