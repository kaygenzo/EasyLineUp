package com.telen.easylineup.domain.model

data class ShirtNumberEntry(
        val number: Int,
        val playerName: String,
        val playerID: Long,
        val eventTime: Long,
        val createdAt: Long,
        val lineupID: Long,
        val lineupName: String
)