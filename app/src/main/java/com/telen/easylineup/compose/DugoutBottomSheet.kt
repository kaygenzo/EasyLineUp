/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** A side sheet's widest; it otherwise takes half the window. */
private val SIDE_SHEET_MAX_WIDTH = 400.dp

/** Below this window width, even in landscape, a side sheet would be too narrow. */
private val SIDE_SHEET_MIN_WINDOW_WIDTH = 520.dp

/**
 * The app's [ModalBottomSheet]: the same, with its [content] scaled to the screen like the
 * screens' ([AdaptiveScale]). A sheet is a window of its own, which the screen's scale doesn't
 * reach, so its text would otherwise be smaller than the screen's under it on a tablet.
 *
 * On a window wider than tall it becomes a side sheet instead, anchored right: a bottom sheet
 * there covers nearly the whole screen and leaves half its own width empty, where a side sheet
 * keeps the screen it acts on visible and gives its list the full height. [sheetState] and
 * [dragHandle] only apply to the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DugoutBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    val windowSize = LocalWindowInfo.current.containerSize
    val windowWidth = with(LocalDensity.current) { windowSize.width.toDp() }
    val wide = windowSize.width > windowSize.height && windowWidth >= SIDE_SHEET_MIN_WINDOW_WIDTH
    if (wide) {
        DugoutSideSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            containerColor = containerColor,
            content = content
        )
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            containerColor = containerColor,
            dragHandle = dragHandle
        ) {
            AdaptiveScale { content() }
        }
    }
}

/**
 * [DugoutBottomSheet] on a wide window: a full-height panel sliding in from the right, at most
 * [SIDE_SHEET_MAX_WIDTH] wide, over a scrim that dismisses it like the bottom sheet's does.
 */
@Composable
private fun DugoutSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val visible = remember { MutableTransitionState(false).apply { targetState = true } }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
        ) {
            AnimatedVisibility(
                visibleState = visible,
                enter = slideInHorizontally { width -> width },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .widthIn(max = SIDE_SHEET_MAX_WIDTH)
                        .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                        .background(containerColor)
                        // Taps inside the sheet stay in it instead of reaching the scrim.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .systemBarsPadding()
                        .padding(top = 16.dp)
                ) {
                    AdaptiveScale { Column { content() } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DugoutPreviews
@Composable
private fun DugoutBottomSheetPreview() {
    DugoutTheme {
        DugoutBottomSheet(onDismissRequest = {}) {
            Text("Sheet content", modifier = Modifier.padding(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DugoutPreviews
@Composable
private fun DugoutSideSheetPreview() {
    DugoutTheme {
        DugoutSideSheet(
            onDismissRequest = {},
            modifier = Modifier,
            containerColor = BottomSheetDefaults.ContainerColor
        ) {
            Text("Sheet content", modifier = Modifier.padding(16.dp))
        }
    }
}
