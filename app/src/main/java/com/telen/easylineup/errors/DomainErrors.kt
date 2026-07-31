/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.errors

/**
 * UI-facing error codes ViewModels translate domain exceptions into, for fragments to turn into
 * the right on-screen message. Not domain models - domain use cases never throw or see these.
 */
class DomainErrors {
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
}
