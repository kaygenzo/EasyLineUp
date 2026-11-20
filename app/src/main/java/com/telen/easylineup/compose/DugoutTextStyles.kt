/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Text roles: one style per kind of text, so the same kind of text has the same size on every
 * screen. A screen picks the role its text plays (`MaterialTheme.typography.rowTitle`), not a
 * size: the screens had drifted apart picking sizes, a player's name in a list ranging from
 * 14 to 18 sp and a sheet's title from 18 to 22 sp depending on the screen.
 */

/** The title of a screen's header. */
val Typography.screenTitle: TextStyle get() = titleMedium

/** The title of a bottom sheet: the same size as a screen's, as a sheet is a screen's step. */
val Typography.sheetTitle: TextStyle get() = titleMedium

/** The muted line under a sheet's [sheetTitle]. */
val Typography.sheetSubtitle: TextStyle get() = bodySmall

/** The name a page is about, at its top: a player's profile, a team's details, the dashboard. */
val Typography.heroTitle: TextStyle get() = titleLarge

/** A row's main line, in any list, sheet or card: a player, a team, a lineup, a setting. */
val Typography.rowTitle: TextStyle get() = titleSmall

/** The line under a row's [rowTitle]. */
val Typography.rowSubtitle: TextStyle get() = bodySmall

/** The title of an empty or failed state, in the middle of a screen or a card. */
val Typography.emptyStateTitle: TextStyle get() = titleMedium

/** The label of a form's or a sheet's button (Save, Cancel, Confirm...). */
val Typography.buttonLabel: TextStyle get() = bodyLarge.copy(fontWeight = FontWeight.Bold)

/** A number in a stat cell, under its label. */
val Typography.statValue: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        fontSize = 19.sp
    )

@DugoutPreviews
@Composable
private fun DugoutTextStylesPreview() {
    DugoutTheme {
        val typography = MaterialTheme.typography
        Column {
            Text("Screen title", style = typography.screenTitle)
            Text("Sheet title", style = typography.sheetTitle)
            Text("Sheet subtitle", style = typography.sheetSubtitle)
            Text("Hero title", style = typography.heroTitle)
            Text("Row title", style = typography.rowTitle)
            Text("Row subtitle", style = typography.rowSubtitle)
            Text("Empty state title", style = typography.emptyStateTitle)
            Text("Button label", style = typography.buttonLabel)
            Text("42", style = typography.statValue)
        }
    }
}
