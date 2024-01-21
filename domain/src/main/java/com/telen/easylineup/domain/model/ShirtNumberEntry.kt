/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property number
 * @property playerName
 * @property playerId
 * @property eventTime
 * @property createdAt
 * @property lineupId
 * @property lineupName
 */
data class ShirtNumberEntry(
    val number: Int,
    val playerName: String,
    val playerId: Long,
    val eventTime: Long,
    val createdAt: Long,
    val lineupId: Long,
    val lineupName: String
)
