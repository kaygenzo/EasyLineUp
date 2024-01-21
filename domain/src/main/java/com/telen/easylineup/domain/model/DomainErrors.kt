/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

class DomainErrors {
    enum class Teams {
        GET_TEAM_FAILED
    }

    enum class Players {
        INVALID_PLAYER_NAME,
        INVALID_PLAYER_ID,
        INVALID_EMAIL_FORMAT,
        INVALID_PHONE_NUMBER_FORMAT,
        ;
    }

    enum class Lineups {
        INVALID_LINEUP_NAME,
        INVALID_TOURNAMENT_NAME,
        ;
    }

    enum class Configuration {
        CANNOT_EXPORT_DATA,
        CANNOT_RETRIEVE_DASHBOARD,
        ;
    }
}
