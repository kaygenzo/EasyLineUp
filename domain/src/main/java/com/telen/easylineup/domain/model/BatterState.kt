/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property playerId
 * @property playerFlag
 * @property playerOrder
 * @property playerName
 * @property playerNumber
 * @property playerPosition
 * @property playerPositionDesc
 * @property canShowPosition
 * @property canMove
 * @property canShowDescription
 * @property canShowOrder
 * @property applyBackground
 * @property isEditable
 */
data class BatterState(
    val playerId: Long,
    val playerFlag: Int,
    var playerOrder: Int,
    val playerName: String,
    val playerNumber: String,
    val playerPosition: FieldPosition,
    val playerPositionDesc: String,
    val canShowPosition: Boolean,
    val canMove: Boolean,
    val canShowDescription: Boolean,
    val canShowOrder: Boolean,
    val applyBackground: Boolean,
    val isEditable: Boolean
)
