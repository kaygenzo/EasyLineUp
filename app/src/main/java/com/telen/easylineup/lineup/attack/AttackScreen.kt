/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.attack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.views.LineupTypeface

/**
 * The batting-order list - single composable for both fixed/view and editable modes (mirrors
 * [AttackFragment]'s own existing "one Fragment, branch on `editable`" design, which the mockup's
 * own screen-note calls out as already correct: "not draggable" and "no handle drawn" are the
 * same fact, not two independently toggled ones).
 */
@Composable
fun AttackScreen(
    batters: List<BatterState>,
    editable: Boolean,
    typeface: LineupTypeface,
    onReorder: (List<BatterState>) -> Unit
) {
    var localBatters by remember { mutableStateOf(batters) }
    var draggedId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetPx by remember { mutableStateOf(0f) }
    var rowHeightPx by remember { mutableStateOf(0f) }

    LaunchedEffect(batters) {
        if (draggedId == null) {
            localBatters = batters
        }
    }

    val fontFamily = lineupTypefaceFontFamily(typeface)
    var showLegend by remember { mutableStateOf(false) }

    if (showLegend) {
        PositionLegendDialog(editable = editable, onDismiss = { showLegend = false })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (editable) {
                EditingOrderBadge()
            } else {
                Box {}
            }
            IconButton(onClick = { showLegend = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_info),
                    contentDescription = stringResource(R.string.attack_legend_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(localBatters, key = { batter -> batter.playerId }) { batter ->
                val isDragged = batter.playerId == draggedId
                BatterRow(
                    batter = batter,
                    editable = editable,
                    fontFamily = fontFamily,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            if (rowHeightPx == 0f) {
                                rowHeightPx = coordinates.size.height.toFloat()
                            }
                        }
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffsetPx else 0f
                        }
                        .then(if (isDragged) Modifier else Modifier.animateItem()),
                    onDragStart = {
                        if (batter.canMove) {
                            draggedId = batter.playerId
                            dragOffsetPx = 0f
                        }
                    },
                    onDrag = { delta ->
                        if (draggedId == null || rowHeightPx == 0f) return@BatterRow
                        dragOffsetPx += delta
                        while (true) {
                            val fromIndex =
                                localBatters.indexOfFirst { it.playerId == draggedId }
                            val direction = if (dragOffsetPx > 0) 1 else -1
                            val targetIndex = fromIndex + direction
                            if (fromIndex == -1 ||
                                targetIndex !in localBatters.indices ||
                                kotlin.math.abs(dragOffsetPx) < rowHeightPx
                            ) {
                                break
                            }
                            val fromBatter = localBatters[fromIndex]
                            val targetBatter = localBatters[targetIndex]
                            if (fromBatter.canMove && targetBatter.canMove) {
                                val swappedFrom =
                                    fromBatter.copy(playerOrder = targetBatter.playerOrder)
                                val swappedTarget =
                                    targetBatter.copy(playerOrder = fromBatter.playerOrder)
                                localBatters = localBatters.toMutableList().apply {
                                    this[fromIndex] = swappedTarget
                                    this[targetIndex] = swappedFrom
                                }
                                onReorder(listOf(swappedFrom, swappedTarget))
                            }
                            dragOffsetPx -= rowHeightPx * direction
                        }
                    },
                    onDragEnd = {
                        draggedId = null
                        dragOffsetPx = 0f
                    }
                )
            }
        }
    }
}

@Composable
private fun EditingOrderBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = stringResource(R.string.attack_editing_order_badge).uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun PositionLegendDialog(editable: Boolean, onDismiss: () -> Unit) {
    val body = stringResource(R.string.attack_legend_body) +
        if (editable) stringResource(R.string.attack_legend_body_editable_suffix) else ""
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.attack_legend_title)) },
        text = { Text(body) }
    )
}

@Composable
private fun lineupTypefaceFontFamily(typeface: LineupTypeface): FontFamily? {
    if (typeface != LineupTypeface.HAND_WRITING) return null
    val context = LocalContext.current
    return remember(typeface) {
        FontFamily(Font("HomemadeApple-Regular.ttf", context.assets))
    }
}

@Composable
private fun BatterRow(
    batter: BatterState,
    editable: Boolean,
    fontFamily: FontFamily?,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val isExtraHitter = batter.playerPosition.isSubstitute() && batter.canShowOrder
    val positionNumber = batter.playerPosition.position.takeIf { batter.canShowPosition }
    val positionDesc = if (batter.canShowPosition || batter.canShowDescription) {
        batter.playerPositionDesc.takeIf { it.isNotBlank() }
    } else {
        null
    }
    val positionText = when {
        positionNumber != null && positionDesc != null -> "$positionNumber · $positionDesc"
        positionNumber != null -> positionNumber.toString()
        else -> positionDesc
    }
    val subtitle = listOfNotNull(
        positionText,
        stringResource(R.string.attack_extra_hitter_suffix).takeIf { isExtraHitter }
    ).joinToString(" · ").takeIf { it.isNotBlank() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(
                if (batter.applyBackground) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (editable) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (batter.canMove) {
                            Modifier.pointerInput(batter.playerId) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { onDragStart() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDrag(dragAmount.y)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragEnd() }
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (batter.canMove) {
                    Icon(
                        painter = painterResource(R.drawable.ic_drag_handle_grip_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (batter.canShowOrder) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = batter.playerOrder.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = batter.playerName,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                style = MaterialTheme.typography.titleMedium
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = batter.playerNumber,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily ?: FontFamily.Monospace,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
