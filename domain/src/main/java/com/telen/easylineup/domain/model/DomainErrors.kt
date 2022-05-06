package com.telen.easylineup.domain.model

class DomainErrors {
    enum class Teams {
        GET_TEAM_FAILED
    }

    enum class Players {
        INVALID_PLAYER_NAME,
        INVALID_PLAYER_ID,
        INVALID_EMAIL_FORMAT,
        INVALID_PHONE_NUMBER_FORMAT
    }

    enum class Lineups {
        DELETE_LINEUP_FAILED,
        LIST_AVAILABLE_PLAYERS_EMPTY,
        SAVE_BATTING_ORDER_FAILED,
        SAVE_LINEUP_MODE_FAILED,
        UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED,
        NEED_ASSIGN_PITCHER_FIRST,
        DP_OR_FLEX_NOT_ASSIGNED,
        INVALID_LINEUP_NAME,
        INVALID_TOURNAMENT_NAME
    }

    enum class PlayerFieldPositions {
        SAVE_PLAYER_FIELD_POSITION_FAILED,
        DELETE_PLAYER_FIELD_POSITION_FAILED
    }

    enum class Configuration {
        CANNOT_EXPORT_DATA,
        CANNOT_RETRIEVE_DASHBOARD
    }
}