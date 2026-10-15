/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.AttrRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.android.material.color.MaterialColors
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

/**
 * Bridges the app's existing XML Material3 theme (AppTheme in themes.xml /
 * values-night/themes.xml, backed by material-colors.xml) into a Compose
 * ColorScheme, so new Compose screens render with the exact same "Dugout
 * Card" palette as the rest of the app instead of duplicating color values.
 *
 * Roles with no dedicated XML token (outlineVariant, the surfaceContainer*
 * tones, scrim, surfaceTint, surface dim/bright) fall back to Compose's
 * baseline M3 defaults, same as the rest of the app, which doesn't define
 * them either.
 */
@Composable
fun DugoutTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // Re-read whenever the configuration (day/night) changes so recreating
    // the ComposeView after a theme flip picks up the new palette.
    val uiMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val colorScheme = remember(uiMode) { context.toDugoutColorScheme() }
    MaterialTheme(colorScheme = colorScheme, typography = DugoutTypography, content = content)
}

/**
 * Shared type scale for the Dugout Card design system - every screen sources its text size and
 * weight from here instead of hardcoding its own, so fixing "this reads small" on one screen
 * doesn't turn into a one-off literal that the next screen has to rediscover on its own. Slots
 * this app doesn't override keep Compose's M3 defaults, so AlertDialog/TextButton/RadioButton
 * (which already read MaterialTheme.typography internally) are unaffected.
 *
 * Deliberately not covered here - these aren't a typographic register, not a missing token:
 * - DugoutAvatar's initials, sized proportionally to the avatar (size.value * 0.33f).
 * - Roster's jersey-number badge (10sp monospace), sized to fit two digits inside a fixed
 *   23dp circular badge rather than to read well as text.
 * - TeamForm's TypeCard emoji (20sp), standing in for an icon, not text content.
 */
private val DugoutTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp),
    labelMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

private fun Context.toDugoutColorScheme(): ColorScheme {
    val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    fun attr(@AttrRes attrRes: Int): Color =
        Color(MaterialColors.getColor(this, attrRes, "DugoutTheme: unresolved attr"))

    return base.copy(
        primary = attr(AppCompatR.attr.colorPrimary),
        onPrimary = attr(MaterialR.attr.colorOnPrimary),
        primaryContainer = attr(MaterialR.attr.colorPrimaryContainer),
        onPrimaryContainer = attr(MaterialR.attr.colorOnPrimaryContainer),
        inversePrimary = attr(MaterialR.attr.colorPrimaryInverse),
        secondary = attr(MaterialR.attr.colorSecondary),
        onSecondary = attr(MaterialR.attr.colorOnSecondary),
        secondaryContainer = attr(MaterialR.attr.colorSecondaryContainer),
        onSecondaryContainer = attr(MaterialR.attr.colorOnSecondaryContainer),
        tertiary = attr(MaterialR.attr.colorTertiary),
        onTertiary = attr(MaterialR.attr.colorOnTertiary),
        tertiaryContainer = attr(MaterialR.attr.colorTertiaryContainer),
        onTertiaryContainer = attr(MaterialR.attr.colorOnTertiaryContainer),
        error = attr(AppCompatR.attr.colorError),
        onError = attr(MaterialR.attr.colorOnError),
        errorContainer = attr(MaterialR.attr.colorErrorContainer),
        onErrorContainer = attr(MaterialR.attr.colorOnErrorContainer),
        background = attr(android.R.attr.colorBackground),
        onBackground = attr(MaterialR.attr.colorOnBackground),
        surface = attr(MaterialR.attr.colorSurface),
        onSurface = attr(MaterialR.attr.colorOnSurface),
        surfaceVariant = attr(MaterialR.attr.colorSurfaceVariant),
        onSurfaceVariant = attr(MaterialR.attr.colorOnSurfaceVariant),
        outline = attr(MaterialR.attr.colorOutline),
        inverseSurface = attr(MaterialR.attr.colorSurfaceInverse),
        inverseOnSurface = attr(MaterialR.attr.colorOnSurfaceInverse),
        surfaceTint = attr(AppCompatR.attr.colorPrimary)
    )
}
