/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutCard
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.ScreenHeader
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.titleRes

data class SettingsScreenState(
    val teamName: String,
    val teamType: TeamType,
    val teamImageUri: Uri?,
    val lineupStyleValue: String,
    val themeValue: String,
    val menColor: Color,
    val womenColor: Color,
    val updateExistingOnImport: Boolean
)

fun currentSettingsScreenState(
    context: android.content.Context,
    teamName: String,
    teamType: TeamType,
    teamImageUri: Uri?
): SettingsScreenState {
    val menColor = SharedPreferencesUtils.getIntSetting(
        context,
        R.string.key_men_style,
        ContextCompat.getColor(context, R.color.default_men_color)
    )
    val womenColor = SharedPreferencesUtils.getIntSetting(
        context,
        R.string.key_women_style,
        ContextCompat.getColor(context, R.color.default_women_color)
    )
    return SettingsScreenState(
        teamName = teamName,
        teamType = teamType,
        teamImageUri = teamImageUri,
        lineupStyleValue = SharedPreferencesUtils.getStringSetting(
            context,
            R.string.key_lineup_style,
            context.getString(R.string.lineup_style_default_value)
        ),
        themeValue = SharedPreferencesUtils.getStringSetting(
            context,
            R.string.key_day_night_theme,
            context.getString(R.string.lineup_theme_default_value)
        ),
        menColor = Color(menColor),
        womenColor = Color(womenColor),
        updateExistingOnImport = androidx.preference.PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.key_import_data_update_object), false)
    )
}

@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    onBackClick: () -> Unit,
    onTeamClick: () -> Unit,
    onLineupStyleSelected: (String) -> Unit,
    onThemeSelected: (String) -> Unit,
    onMenColorPicked: (Int) -> Unit,
    onWomenColorPicked: (Int) -> Unit,
    onUpdateExistingChanged: (Boolean) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onPlayStoreClick: () -> Unit
) {
    var showLineupStyleDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        ScreenHeader(
            icon = R.drawable.ic_arrow_left_white_24dp,
            onIconClick = onBackClick,
            iconContentDescription = stringResource(R.string.generic_back),
            eyebrow = stringResource(R.string.screen_header_eyebrow_app),
            title = stringResource(R.string.title_settings),
            modifier = Modifier.padding(bottom = 14.dp)
        )

        SectionLabel(stringResource(R.string.settings_category_team))
        DugoutCard {
            val teamTypeLabel = if (state.teamType.titleRes() != 0) {
                stringResource(state.teamType.titleRes()) + " — "
            } else {
                ""
            }
            SettingsRow(
                leading = {
                    DugoutAvatar(
                        name = state.teamName,
                        imageUri = state.teamImageUri,
                        size = 34.dp
                    )
                },
                title = state.teamName,
                subtitle = teamTypeLabel + stringResource(R.string.settings_team_row_subtitle),
                onClick = onTeamClick,
                trailing = { ChevronIcon() }
            )
        }

        SectionLabel(stringResource(R.string.settings_category_style))
        DugoutCard {
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_font) },
                title = stringResource(R.string.setting_lineup_style_description),
                subtitle = arrayLabel(
                    R.array.pref_lineup_font_style_labels,
                    R.array.pref_lineup_font_style_values,
                    state.lineupStyleValue
                ),
                onClick = { showLineupStyleDialog = true },
                trailing = { ChevronIcon() }
            )
            RowDivider()
            ColorSwatchRow(
                title = stringResource(R.string.setting_men_style_description),
                color = state.menColor,
                chipTint = MaterialTheme.colorScheme.primary,
                onColorPicked = onMenColorPicked
            )
            RowDivider()
            ColorSwatchRow(
                title = stringResource(R.string.setting_women_style_description),
                color = state.womenColor,
                chipTint = MaterialTheme.colorScheme.secondary,
                onColorPicked = onWomenColorPicked
            )
            RowDivider()
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_theme) },
                title = stringResource(R.string.setting_day_night_style_description),
                subtitle = arrayLabel(
                    R.array.pref_theme_style_labels,
                    R.array.pref_theme_style_values,
                    state.themeValue
                ),
                onClick = { showThemeDialog = true },
                trailing = { ChevronIcon() }
            )
        }

        SectionLabel(stringResource(R.string.settings_category_data))
        DugoutCard {
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_export) },
                title = stringResource(R.string.setting_export_data_description),
                subtitle = stringResource(R.string.setting_export_data_subtitle),
                onClick = onExportClick,
                trailing = { ChevronIcon() }
            )
            RowDivider()
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_import) },
                title = stringResource(R.string.setting_import_data_description),
                subtitle = stringResource(R.string.setting_import_data_subtitle),
                onClick = onImportClick,
                trailing = { ChevronIcon() }
            )
            RowDivider()
            SettingsRow(
                title = stringResource(R.string.setting_import_data_update_object_description),
                trailing = {
                    Switch(
                        checked = state.updateExistingOnImport,
                        onCheckedChange = onUpdateExistingChanged
                    )
                }
            )
            RowDivider()
            SettingsRow(
                leading = {
                    RowIconChip(
                        R.drawable.ic_delete_24dp,
                        tint = MaterialTheme.colorScheme.error,
                        background = MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                    )
                },
                title = stringResource(R.string.setting_delete_data_description),
                subtitle = stringResource(R.string.setting_delete_data_subtitle),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteAllDataClick,
                trailing = { ChevronIcon(tint = MaterialTheme.colorScheme.error) }
            )
        }

        SectionLabel(stringResource(R.string.setting_category_about_title))
        DugoutCard {
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_info) },
                title = stringResource(R.string.setting_version_title),
                trailing = {
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            RowDivider()
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_licenses) },
                title = stringResource(R.string.setting_licenses_title),
                onClick = onLicensesClick,
                trailing = { ChevronIcon() }
            )
            RowDivider()
            SettingsRow(
                leading = { RowIconChip(R.drawable.ic_settings_star) },
                title = stringResource(R.string.setting_play_store_title),
                onClick = onPlayStoreClick,
                trailing = { ChevronIcon() }
            )
        }
    }

    if (showLineupStyleDialog) {
        SingleChoiceDialog(
            titleRes = R.string.setting_lineup_style_description,
            labelsRes = R.array.pref_lineup_font_style_labels,
            valuesRes = R.array.pref_lineup_font_style_values,
            currentValue = state.lineupStyleValue,
            onSelected = {
                onLineupStyleSelected(it)
                showLineupStyleDialog = false
            },
            onDismiss = { showLineupStyleDialog = false }
        )
    }

    if (showThemeDialog) {
        SingleChoiceDialog(
            titleRes = R.string.setting_day_night_style_description,
            labelsRes = R.array.pref_theme_style_labels,
            valuesRes = R.array.pref_theme_style_values,
            currentValue = state.themeValue,
            onSelected = {
                onThemeSelected(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun arrayLabel(labelsRes: Int, valuesRes: Int, value: String): String {
    val labels = stringArrayResource(labelsRes)
    val values = stringArrayResource(valuesRes)
    val index = values.indexOf(value)
    return if (index >= 0) labels[index] else ""
}

@Composable
private fun SingleChoiceDialog(
    titleRes: Int,
    labelsRes: Int,
    valuesRes: Int,
    currentValue: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val labels = stringArrayResource(labelsRes)
    val values = stringArrayResource(valuesRes)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            Column {
                labels.forEachIndexed { index, label ->
                    val value = values[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(value) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = value == currentValue,
                            onClick = { onSelected(value) }
                        )
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    leading: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leading != null) {
            leading()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = titleColor, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        trailing()
    }
}

@Composable
private fun RowIconChip(
    iconRes: Int,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    background: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ColorSwatchRow(
    title: String,
    color: Color,
    chipTint: Color,
    onColorPicked: (Int) -> Unit
) {
    val context = LocalContext.current
    SettingsRow(
        leading = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(chipTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(color)
                )
            }
        },
        title = title,
        onClick = {
            ColorPickerDialog.Builder(context)
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setPositiveButton(
                    context.getString(android.R.string.ok),
                    ColorEnvelopeListener { envelope, _ -> onColorPicked(envelope.color) }
                )
                .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .also { it.colorPickerView.setInitialColor(color.toArgb()) }
                .show()
        },
        trailing = { ChevronIcon() }
    )
}

@Composable
private fun ChevronIcon(tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_right_white_24dp),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun RowDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@DugoutPreviews
@Composable
private fun SettingsScreenPreview() {
    DugoutTheme {
        SettingsScreen(
            state = SettingsScreenState(
                teamName = "Riverside Hawks",
                teamType = TeamType.BASEBALL,
                teamImageUri = null,
                lineupStyleValue = "0",
                themeValue = "0",
                menColor = Color(0xFFD8324A),
                womenColor = Color(0xFFE8B93F),
                updateExistingOnImport = false
            ),
            onBackClick = {},
            onTeamClick = {},
            onLineupStyleSelected = {},
            onThemeSelected = {},
            onMenColorPicked = {},
            onWomenColorPicked = {},
            onUpdateExistingChanged = {},
            onExportClick = {},
            onImportClick = {},
            onDeleteAllDataClick = {},
            onLicensesClick = {},
            onPlayStoreClick = {}
        )
    }
}
