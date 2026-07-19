package com.telen.easylineup.utils

import android.content.Context
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.StringResourcesProvider

class StringResourcesProviderImpl(private val context: Context) : StringResourcesProvider {
    override fun positionShortNames(teamType: Int): Array<String> {
        return if (teamType == TeamType.SOFTBALL.id) {
            context.resources.getStringArray(R.array.field_positions_softball_list)
        } else {
            context.resources.getStringArray(R.array.field_positions_baseball_list)
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
