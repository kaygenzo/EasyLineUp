/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.swap

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutAvatar
import com.telen.easylineup.compose.DugoutPreviews
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.compose.dashedBorder
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.titleRes

@Composable
fun SwitchTeamSheetContent(
    teams: List<Team>,
    onTeamClick: (Team) -> Unit,
    onCreateTeamClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.switch_team_sheet_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = pluralStringResource(
                R.plurals.switch_team_sheet_subtitle,
                teams.size,
                teams.size
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (teams.isEmpty()) {
            Text(
                text = stringResource(R.string.team_list_empty_text),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            teams.forEach { team ->
                TeamRow(team = team, onClick = { onTeamClick(team) })
                RowDivider()
            }
        }

        CreateTeamRow(onClick = onCreateTeamClick)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun TeamRow(team: Team, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DugoutAvatar(name = team.name, imageUri = team.image?.let(Uri::parse), size = 44.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = team.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
            val teamType = TeamType.getTypeById(team.type)
            if (teamType.titleRes() != 0) {
                Text(
                    text = stringResource(teamType.titleRes()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (team.main) {
            CurrentPill()
        }
    }
}

@Composable
private fun CurrentPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.switch_team_sheet_current).uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun CreateTeamRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .dashedBorder(MaterialTheme.colorScheme.outline, 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_white_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.switch_team_sheet_create_row),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@DugoutPreviews
@Composable
private fun SwitchTeamSheetContentPreview() {
    DugoutTheme {
        SwitchTeamSheetContent(
            teams = listOf(
                Team(id = 1, name = "Riverside Hawks", type = TeamType.BASEBALL.id, main = true),
                Team(id = 2, name = "Riverside Storm", type = TeamType.SOFTBALL.id),
                Team(id = 3, name = "Eastside Comets", type = TeamType.BASEBALL_5.id)
            ),
            onTeamClick = {},
            onCreateTeamClick = {}
        )
    }
}
