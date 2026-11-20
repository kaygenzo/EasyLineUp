/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Text roles: one style per kind of text, so the same kind of text has the same size, weight and
 * font on every screen. A screen picks the role its text plays
 * (`MaterialTheme.typography.rowTitle`), never a size, a weight or a font of its own: the screens
 * had drifted apart doing so, a player's name in a list ranging from 14 to 18 sp, a form field's
 * label from 11 to 14 sp and a jersey number from 14 to 18 sp depending on the screen.
 *
 * A new kind of text gets a new role here. The roles point to the slots of DugoutTypography, or
 * to a style of their own where no slot fits (a monospace number).
 */

private const val INITIALS_SIZE_RATIO = 0.33f
private const val BADGE_NUMBER_SIZE_RATIO = 0.43f

private fun TextStyle.monospace(): TextStyle = copy(fontFamily = FontFamily.Monospace)

// Titles

/** The welcome screen's headline: the biggest text of the app. */
val Typography.displayTitle: TextStyle get() = displaySmall

/** The title of a screen's header. */
val Typography.screenTitle: TextStyle get() = titleMedium

/**
 * The smallest a [screenTitle] shrinks to when a long translation wouldn't fit on one line next
 * to the header's actions: only then, so every title that fits keeps the role's size.
 */
val SCREEN_TITLE_MIN_FONT_SIZE = 12.sp

/** The title of a bottom sheet: the same size as a screen's, as a sheet is a screen's step. */
val Typography.sheetTitle: TextStyle get() = titleMedium

/** The title of an alert dialog: the same size as a sheet's, which most dialogs became. */
val Typography.dialogTitle: TextStyle get() = titleMedium

/** The name a page is about, at its top: a player's profile, a team's details, the dashboard. */
val Typography.heroTitle: TextStyle get() = titleLarge

/** The title of an empty or failed state, in the middle of a screen or a card. */
val Typography.emptyStateTitle: TextStyle get() = titleMedium

// Around titles

/** The small uppercase line above a title: the team above a screen's name, the app's name. */
val Typography.eyebrow: TextStyle get() = labelMedium

/** The muted line under a sheet's [sheetTitle]. */
val Typography.sheetSubtitle: TextStyle get() = bodySmall

/** The muted line under a [heroTitle]. */
val Typography.heroSubtitle: TextStyle get() = bodySmall

/** The uppercase label above a section or a card's content. */
val Typography.sectionLabel: TextStyle get() = labelMedium

/** A file's name, under a sheet's title: monospace, as it reads like a path. */
val Typography.fileName: TextStyle get() = bodySmall.monospace()

// Rows

/** A row's main line, in any list, sheet or card: a player, a team, a lineup, a setting. */
val Typography.rowTitle: TextStyle get() = titleSmall

/** The line under a row's [rowTitle]. */
val Typography.rowSubtitle: TextStyle get() = bodySmall

/** A date or a time in a row, where its digits line up from one row to the next. */
val Typography.rowMeta: TextStyle get() = bodySmall.monospace()

/** A one-character glyph standing for an icon at a row's edge: a "+" or a "›". */
val Typography.rowGlyph: TextStyle get() = titleMedium

// Running text

/** A sentence or a paragraph: a sheet's question, an empty state's explanation. */
val Typography.message: TextStyle get() = bodyMedium

/** The small print at the bottom of a sheet or a section. */
val Typography.footnote: TextStyle get() = bodySmall

// Forms

/** The uppercase label above a form's field. */
val Typography.fieldLabel: TextStyle get() = labelSmall

/** What the user typed or picked in a field. */
val Typography.fieldText: TextStyle get() = bodyLarge.copy(fontWeight = FontWeight.SemiBold)

/** A field's placeholder, until something is typed or picked. */
val Typography.fieldPlaceholder: TextStyle get() = bodyLarge

/** The line under a field: its hint or its error. */
val Typography.fieldSupport: TextStyle get() = bodySmall

/** An option of a segmented control (a strategy, a count): values, hence monospace. */
val Typography.segmentLabel: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.Bold).monospace()

// Actions

/** The label of a form's or a sheet's button (Save, Cancel, Confirm...), or a text action. */
val Typography.buttonLabel: TextStyle get() = bodyLarge.copy(fontWeight = FontWeight.Bold)

/** The label of a header's pill button (Edit, Save the lineup). */
val Typography.headerActionLabel: TextStyle get() = labelMedium

/** A tab's label: the bottom navigation's, the lineup's Defense/Offense tabs. */
val Typography.tabLabel: TextStyle get() = labelSmall

/** The text of a pill, a badge or a chip: a status, a sort, a strategy, a position. */
val Typography.chipLabel: TextStyle get() = labelSmall

// Numbers

/** The uppercase label of a stat or an info cell. */
val Typography.statLabel: TextStyle get() = labelSmall

/** A number in a stat cell, under its [statLabel]. */
val Typography.statValue: TextStyle
    get() = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        fontSize = 19.sp
    )

/** The one big number of a dashboard tile. */
val Typography.tileValue: TextStyle get() = displaySmall.monospace()

/** A text value in an info cell, under its [statLabel]. */
val Typography.infoValue: TextStyle get() = bodyMedium.copy(fontWeight = FontWeight.Bold)

/** A numeric value in an info cell (a height, a license number): monospace. */
val Typography.infoNumber: TextStyle
    get() = bodyLarge.copy(fontWeight = FontWeight.Bold).monospace()

/** A player's jersey number, wherever it shows next to their name. */
val Typography.jerseyNumber: TextStyle
    get() = titleSmall.copy(fontWeight = FontWeight.Bold).monospace()

/** A batter's place in the batting order. */
val Typography.battingOrder: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.Bold).monospace()

/** The jersey number typed in the dashboard's lookup box. */
val Typography.numberInput: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.Bold).monospace()

/** A chart's axis labels and bar values. */
val Typography.chartLabel: TextStyle get() = labelSmall

/** The initials or the DP/FLEX label on a field position's marker. */
val Typography.markerLabel: TextStyle get() = labelSmall.copy(fontWeight = FontWeight.Bold)

/** A player's initials in an avatar or a field marker [containerSize] wide: a third of it. */
fun Typography.avatarInitials(containerSize: Dp): TextStyle =
    labelSmall.copy(fontSize = (containerSize.value * INITIALS_SIZE_RATIO).sp, letterSpacing = 0.sp)

/** A jersey number in a round badge [badgeSize] wide, sized for two digits to fit. */
fun Typography.badgeNumber(badgeSize: Dp): TextStyle = labelSmall.copy(
    fontSize = (badgeSize.value * BADGE_NUMBER_SIZE_RATIO).sp,
    letterSpacing = 0.sp
).monospace()

/** An emoji standing for an icon, as on the team type cards. */
val Typography.emojiIcon: TextStyle get() = TextStyle(fontSize = 20.sp)

// Dates

/** The day of a tournament ticket's date chip. */
val Typography.dateDay: TextStyle get() = titleLarge.copy(fontWeight = FontWeight.ExtraBold)

/** The month under a [dateDay]. */
val Typography.dateMonth: TextStyle get() = labelMedium

/** The day of a compact date chip, in a picker's row. */
val Typography.compactDateDay: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.ExtraBold)

/** The month under a [compactDateDay]. */
val Typography.compactDateMonth: TextStyle get() = labelSmall

@DugoutPreviews
@Composable
private fun DugoutTextStylesPreview() {
    DugoutTheme {
        val t = MaterialTheme.typography
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            listOf(
                "Display title" to t.displayTitle,
                "Screen title" to t.screenTitle,
                "Sheet title" to t.sheetTitle,
                "Dialog title" to t.dialogTitle,
                "Hero title" to t.heroTitle,
                "Empty state title" to t.emptyStateTitle,
                "EYEBROW" to t.eyebrow,
                "Sheet subtitle" to t.sheetSubtitle,
                "Hero subtitle" to t.heroSubtitle,
                "SECTION LABEL" to t.sectionLabel,
                "Row title" to t.rowTitle,
                "Row subtitle" to t.rowSubtitle,
                "12/10/2026" to t.rowMeta,
                "›" to t.rowGlyph,
                "Message" to t.message,
                "Footnote" to t.footnote,
                "FIELD LABEL" to t.fieldLabel,
                "Field text" to t.fieldText,
                "Field placeholder" to t.fieldPlaceholder,
                "Field support" to t.fieldSupport,
                "Segment" to t.segmentLabel,
                "Button label" to t.buttonLabel,
                "Header action" to t.headerActionLabel,
                "TAB" to t.tabLabel,
                "CHIP" to t.chipLabel,
                "STAT LABEL" to t.statLabel,
                "42" to t.statValue,
                "7" to t.tileValue,
                "Info value" to t.infoValue,
                "1.80" to t.infoNumber,
                "23" to t.jerseyNumber,
                "1" to t.battingOrder,
                "99" to t.numberInput,
                "Chart" to t.chartLabel,
                "AB" to t.markerLabel,
                "9" to t.dateDay,
                "OCT" to t.dateMonth,
                "9" to t.compactDateDay,
                "OCT" to t.compactDateMonth,
                "file.json" to t.fileName,
                "JA" to t.avatarInitials(40.dp),
                "07" to t.badgeNumber(23.dp),
                "⚾" to t.emojiIcon
            ).forEach { (text, style) -> Text(text, style = style) }
        }
    }
}
