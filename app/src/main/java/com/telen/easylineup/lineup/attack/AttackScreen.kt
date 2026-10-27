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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
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
                        val fromIndex = localBatters.indexOfFirst { it.playerId == draggedId }
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
    val positionText = if (batter.canShowPosition || batter.canShowDescription) {
        batter.playerPositionDesc.takeIf { it.isNotBlank() }
    } else {
        null
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (editable) {
            Box(
                modifier = Modifier
                    .size(24.dp)
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
                        painter = painterResource(R.drawable.ic_reorder_24dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (batter.canShowOrder) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = batter.playerOrder.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = batter.playerName,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                style = MaterialTheme.typography.titleSmall
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    RoundedCornerShape(9.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = batter.playerNumber,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily ?: FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
