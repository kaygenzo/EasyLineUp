/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Landscape layouts. A screen decides on the room it is given, never on the orientation: a
 * BoxWithConstraints around it, and [isWide] picks the side-by-side layout, so a tablet in
 * portrait, a split screen or a detail pane get the layout that fits them.
 */

/** Whether this box is wider than tall: its panes then go side by side instead of stacking. */
val BoxWithConstraintsScope.isWide: Boolean get() = maxWidth > maxHeight

/** The gap between the two panes of a [TwoPaneRow]. */
val PANE_GAP = 16.dp

/** A [TwoPaneRow]'s side margin: the same as the screen headers', so the edges line up. */
val PANE_EDGE = 20.dp

/**
 * Two panes side by side, each scrolling on its own: what a portrait screen stacks in one
 * scrolling column, a wide screen shows as two, both visible at once. [firstWeight] and
 * [secondWeight] split the width; [spacing] separates a pane's children (0 when they space
 * themselves, as a form's fields do).
 */
@Composable
fun TwoPaneRow(
    modifier: Modifier = Modifier,
    firstWeight: Float = 1f,
    secondWeight: Float = 1f,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spacing: Dp = 12.dp,
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PANE_EDGE),
        horizontalArrangement = Arrangement.spacedBy(PANE_GAP)
    ) {
        PaneColumn(Modifier.weight(firstWeight), verticalAlignment, spacing, first)
        PaneColumn(Modifier.weight(secondWeight), verticalAlignment, spacing, second)
    }
}

@Composable
private fun PaneColumn(
    modifier: Modifier,
    verticalAlignment: Alignment.Vertical,
    spacing: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = PANE_GAP),
        verticalArrangement = when (verticalAlignment) {
            Alignment.CenterVertically -> Arrangement.spacedBy(spacing, Alignment.CenterVertically)
            else -> Arrangement.spacedBy(spacing)
        },
        content = content
    )
}

/**
 * A header's filled action pill (Edit, Save): where a wide screen puts the Save its portrait
 * layout pins to a bottom bar, so no bar takes the little height a landscape phone has.
 */
@Composable
fun HeaderPill(label: String, onClick: () -> Unit, showChevron: Boolean = false) {
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
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headerActionLabel
        )
        if (showChevron) {
            Text(
                text = "›",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headerActionLabel
            )
        }
    }
}

@DugoutPreviews
@Composable
private fun TwoPaneRowPreview() {
    DugoutTheme {
        TwoPaneRow(
            first = { Text("First pane", style = MaterialTheme.typography.rowTitle) },
            second = { Text("Second pane", style = MaterialTheme.typography.rowTitle) }
        )
    }
}

@DugoutPreviews
@Composable
private fun HeaderPillPreview() {
    DugoutTheme {
        HeaderPill(label = "Save", onClick = {})
    }
}
