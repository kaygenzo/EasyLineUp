package com.telen.easylineup.repository.model

class Constants {
    companion object {

        const val APPLICATION_PREFERENCES = "easylineup_prefs"
        const val PREF_FEATURE_SHOW_NEW_SWAP_TEAM = "pref_show_new_swap_team"
        const val PREF_FEATURE_SHOW_NEW_ROASTER = "pref_show_new_roaster"

        const val LINEUP_ID = "lineup_id"
        const val PLAYER_ID = "player_id"
        const val LINEUP_TITLE = "lineup_title"

        //extras
        const val EXTRA_EDITABLE = "com.telen.extra.editable"
        const val EXTRA_CLICKABLE = "com.telen.extra.clickable"
        const val EXTRA_TEAM = "com.telen.extra.team"
        const val EXTRA_CAN_EXIT = "com.telen.extra.can_exit"

        const val NAME = "_name"
        const val IMAGE = "_image"
        const val PLAYER_SHIRT = "player_shirtNumber"
        const val PLAYER_LICENSE = "player_licenseNumber"
        const val PLAYER_POSITIONS = "player_positions"

        const val MIN_PLAYER_COUNT = 9

        const val SUBSTITUTE_ORDER_VALUE = 200

        const val ORDER_PITCHER_WHEN_DH = 10

        const val TYPE_LAST_LINEUP = 0
        const val TYPE_TEAM_SIZE = 1
        const val TYPE_MOST_USED_PLAYER = 2
        const val TYPE_SHAKE_BETA = 3
    }
}