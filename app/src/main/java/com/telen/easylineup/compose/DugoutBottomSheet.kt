/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The app's [ModalBottomSheet]: the same, with its [content] scaled to the screen like the
 * screens' ([AdaptiveScale]). A sheet is a window of its own, which the screen's scale doesn't
 * reach, so its text would otherwise be smaller than the screen's under it on a tablet.
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
