package com.telen.easylineup.utils

import android.content.Context
import com.telen.easylineup.R
import com.telen.easylineup.domain.R as DomainR
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.StringResourcesProvider

class StringResourcesProviderImpl(private val context: Context) : StringResourcesProvider {
    override fun positionShortNames(teamType: Int): Array<String> {
        // These 2 arrays still live in domain/res, not app/res: FieldPosition.kt (domain)
        // still reads them too (chantier 7, not yet done), so they can't move to app yet.
        return if (teamType == TeamType.SOFTBALL.id) {
            context.resources.getStringArray(DomainR.array.field_positions_softball_list)
        } else {
            context.resources.getStringArray(DomainR.array.field_positions_baseball_list)
        }
    }

    override fun unknownPlayerName(): String {
        return context.getString(R.string.tournament_stats_unknown_player_name)
    }

    override fun gamesPlayedLabel(): String {
        return context.getString(R.string.tournament_stats_label_games_played)
    }

    override fun strategyDisplayNames(teamType: Int): Array<String>? {
        return TeamType.getTypeById(teamType).getStrategiesDisplayName(context)
    }
}
