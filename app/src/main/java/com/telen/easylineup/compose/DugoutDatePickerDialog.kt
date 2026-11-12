/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.utils.localTimeToPickerDate
import com.telen.easylineup.utils.pickerDateToLocalTime

/** Below this height the calendar grid does not fit: the dialog opens on keyboard input. */
private const val COMPACT_HEIGHT_DP = 480

/**
 * Material3 date picker dialog working in the app's own convention: every millis in and out is
 * a local time ([initialDateMillis], the optional inclusive [firstSelectableDateMillis] and
 * [lastSelectableDateMillis]), and [onDateSelected] receives the local midnight of the day
 * picked. The UTC-midnight convention of the picker itself stays inside (see PickerDates.kt).
 *
 * It replaces the View-based MaterialDatePicker, whose listeners were lambdas a rotation lost:
 * the Fragment manager restored the dialog, but picking a date in it did nothing. The caller
 * keeps whether the dialog is open in its own state, and the picker's state is saveable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DugoutDatePickerDialog(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    firstSelectableDateMillis: Long? = null,
    lastSelectableDateMillis: Long? = null
) {
    val selectableDates = remember(firstSelectableDateMillis, lastSelectableDateMillis) {
        val first = firstSelectableDateMillis?.let { localTimeToPickerDate(it) }
        val last = lastSelectableDateMillis?.let { localTimeToPickerDate(it) }
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return (first == null || utcTimeMillis >= first) &&
                    (last == null || utcTimeMillis <= last)
            }
        }
    }
    val windowHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    val compact = windowHeight < COMPACT_HEIGHT_DP.dp
    val state = rememberDatePickerState(
        initialSelectedDateMillis = localTimeToPickerDate(initialDateMillis),
        initialDisplayMode = if (compact) DisplayMode.Input else DisplayMode.Picker,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { onDateSelected(pickerDateToLocalTime(it)) }
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@DugoutPreviews
@Composable
private fun DugoutDatePickerDialogPreview() {
    DugoutTheme {
        DugoutDatePickerDialog(
            // Wednesday, November 11, 2026, in a Nov 10 - Nov 12 window.
            initialDateMillis = 1_794_355_200_000L,
            onDateSelected = {},
            onDismiss = {},
            firstSelectableDateMillis = 1_794_268_800_000L,
            lastSelectableDateMillis = 1_794_441_600_000L
        )
    }
}
