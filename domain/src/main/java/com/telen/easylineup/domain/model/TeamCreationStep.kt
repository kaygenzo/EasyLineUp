/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property id
 */
enum class TeamCreationStep(val id: Int) {
    TEAM(0),
    TYPE(1),
    FINISH(2),
    CANCEL(3),
    ;

    companion object {
        fun getStepById(id: Int): TeamCreationStep? {
            return values().firstOrNull { it.id == id }
        }
    }
}
