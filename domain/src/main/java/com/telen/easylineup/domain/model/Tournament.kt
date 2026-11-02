/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.TournamentExport
import java.io.Serializable
import java.util.UUID

/**
 * @property id
 * @property name
 * @property createdAt
 * @property startTime
 * @property endTime
 * @property address
 * @property hash
 */
data class Tournament(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val startTime: Long,
    val endTime: Long,
    val address: String? = null,
    val hash: String? = UUID.randomUUID().toString()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Tournament

        if (id != other.id) {
            return false
        }
        if (name != other.name) {
            return false
        }
        if (createdAt != other.createdAt) {
            return false
        }
        if (startTime != other.startTime) {
            return false
        }
        if (endTime != other.endTime) {
            return false
        }
        if (address != other.address) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        return result
    }
}

/** Whether this tournament has an explicit start/end date window, rather than none at all. */
fun Tournament.hasDateWindow(): Boolean {
    return startTime > 0 && endTime > 0
}

fun Tournament.toTournamentExport(lineups: List<LineupExport>): TournamentExport {
    return TournamentExport(
        hash ?: UUID.randomUUID().toString(),
        name,
        createdAt,
        startTime,
        endTime,
        address,
        lineups
    )
}
