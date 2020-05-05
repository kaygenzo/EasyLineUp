package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.TournamentExport
import java.io.Serializable
import java.util.*

data class Tournament (
        var id: Long = 0,
        val name: String,
        var createdAt: Long,
        var hash: String? = UUID.randomUUID().toString()
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tournament

        if (id != other.id) return false
        if (name != other.name) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

fun Tournament.toTournamentExport(lineups: List<LineupExport>): TournamentExport {
    return TournamentExport(hash ?: UUID.randomUUID().toString(), name, createdAt, lineups)
}