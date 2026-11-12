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
        title = { Text(stringResource(R.string.setting_licenses_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.libraries_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(libraries, key = { it.name }) { library ->
                        LibraryRow(library = library, onClick = { onLibraryClick(library) })
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
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

@Composable
private fun LibraryRow(library: OpenSourceLibrary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(text = library.name, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = library.license.displayName,
            style = MaterialTheme.typography.bodySmall,
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
