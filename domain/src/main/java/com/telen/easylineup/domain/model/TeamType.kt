/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property id
 * @property position
 * @property defaultStrategy
 */
enum class TeamType(
    val id: Int,
    val position: Int,
    val defaultStrategy: TeamStrategy
) {
    UNKNOWN(0, -1, TeamStrategy.STANDARD),
    BASEBALL(1, 0, TeamStrategy.STANDARD),
    SOFTBALL(2, 1, TeamStrategy.STANDARD),
    BASEBALL_5(3, 2, TeamStrategy.B5_DEFAULT),
    ;

    fun getStrategies(): Array<TeamStrategy> {
        return when (this) {
            SOFTBALL -> arrayOf(
                TeamStrategy.STANDARD,
                TeamStrategy.FIVE_MAN_STANDARD,
                TeamStrategy.SLOWPITCH
            )

            BASEBALL_5 -> arrayOf(TeamStrategy.B5_DEFAULT)
            else -> arrayOf(TeamStrategy.STANDARD, TeamStrategy.FIVE_MAN_STANDARD)
        }
    }

    companion object {
        fun getTypeById(id: Int): TeamType {
            values().forEach {
                if (it.id == id) {
                    return it
                }
            }
            return UNKNOWN
        }
    }
}
