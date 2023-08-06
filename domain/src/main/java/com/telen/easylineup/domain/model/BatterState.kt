package com.telen.easylineup.domain.model

data class BatterState(
    val playerID: Long,
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