/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.license

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.buttonLabel
import com.telen.easylineup.compose.dialogTitle
import com.telen.easylineup.compose.message
import com.telen.easylineup.compose.rowSubtitle
import com.telen.easylineup.compose.rowTitle

/**
 * The open source libraries the app ships with, from Settings. Tapping one opens its project
 * page through [onLibraryClick]; the list scrolls inside the dialog on a small screen.
 */
@Composable
fun LicensesDialog(
    libraries: List<OpenSourceLibrary>,
    onLibraryClick: (OpenSourceLibrary) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.setting_licenses_title),
                style = MaterialTheme.typography.dialogTitle
            )
        },
        text = {
            LicensesList(
                libraries = libraries,
                onLibraryClick = onLibraryClick,
                listModifier = Modifier.heightIn(max = 360.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.buttonLabel
                )
            }
        }
    )
}

@DugoutPreviews
@Composable
private fun LicensesDialogPreview() {
    DugoutTheme {
        LicensesDialog(libraries = openSourceLibraries, onLibraryClick = {}, onDismiss = {})
    }
}

/**
 * The libraries and what they are: [LicensesDialog]'s content, and Settings' detail pane on a
 * wide screen, where it shows next to the list instead of in a dialog over it.
 */
@Composable
fun LicensesList(
    libraries: List<OpenSourceLibrary>,
    onLibraryClick: (OpenSourceLibrary) -> Unit,
    modifier: Modifier = Modifier,
    listModifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.libraries_title),
            style = MaterialTheme.typography.message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(modifier = listModifier) {
            items(libraries, key = { it.name }) { library ->
                LibraryRow(library = library, onClick = { onLibraryClick(library) })
                HorizontalDivider()
            }
        }
    }
}

@DugoutPreviews
@Composable
private fun LicensesListPreview() {
    DugoutTheme {
        LicensesList(libraries = openSourceLibraries, onLibraryClick = {})
    }
}

@Composable
private fun LibraryRow(library: OpenSourceLibrary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(text = library.name, style = MaterialTheme.typography.rowTitle)
        Text(
            text = library.license.displayName,
            style = MaterialTheme.typography.rowSubtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@DugoutPreviews
@Composable
private fun LibraryRowPreview() {
    DugoutTheme {
        LibraryRow(library = openSourceLibraries.first(), onClick = {})
    }
}
