package com.telen.easylineup.repository.model

class Constants {
    companion object {

        private const val BASE_ROOT_DIRECTORY = "LineupKeeper"
        const val EXPORTS_DIRECTORY = "$BASE_ROOT_DIRECTORY/exports/"
        const val LINEUPS_DIRECTORY = "$BASE_ROOT_DIRECTORY/lineups/"

        const val APPLICATION_PREFERENCES = "easylineup_prefs"
        const val PREF_FEATURE_SHOW_NEW_SWAP_TEAM = "pref_show_new_swap_team"
        const val PREF_FEATURE_SHOW_NEW_ROSTER = "pref_show_new_roster"

        const val LINEUP_ID = "lineup_id"
        const val PLAYER_ID = "player_id"
        const val LINEUP_TITLE = "lineup_title"

        //extras
        const val EXTRA_EDITABLE = "com.telen.extra.editable"
        const val EXTRA_CLICKABLE = "com.telen.extra.clickable"
        const val EXTRA_TEAM = "com.telen.extra.team"
        const val EXTRA_CAN_EXIT = "com.telen.extra.can_exit"
        const val EXTRA_TEAM_COUNT = "com.telen.extra.team_count"

        const val EXTRA_TRAINING_CARD = "com.telen.extra.training_card"

        const val NAME = "_name"
        const val IMAGE = "_image"
        const val PLAYER_SHIRT = "player_shirtNumber"
        const val PLAYER_LICENSE = "player_licenseNumber"
        const val PLAYER_POSITIONS = "player_positions"

        const val EXTRA_TOURNAMENT = "com.telen.extra.tournament"

        const val MIN_PLAYER_COUNT = 9

        const val SUBSTITUTE_ORDER_VALUE = 200

        const val ORDER_PITCHER_WHEN_DH = 10

        const val TYPE_LAST_LINEUP = 0
        const val TYPE_TEAM_SIZE = 1
        const val TYPE_MOST_USED_PLAYER = 2
        const val TYPE_SHAKE_BETA = 3

        const val STATUS_ALL = 0
        const val STATUS_PARTIAL = 1
    }
}