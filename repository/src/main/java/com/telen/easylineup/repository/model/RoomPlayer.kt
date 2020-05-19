package com.telen.easylineup.repository.model

import androidx.room.*
import com.telen.easylineup.domain.model.Player
import java.util.*

@Entity(
        tableName = "players",
        indices = [Index(value = ["name", "licenseNumber"])],
        foreignKeys = [
            ForeignKey(entity = RoomTeam::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
internal data class RoomPlayer(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "teamID") var teamId: Long = 0,
        @ColumnInfo(name = "name") var name: String = "",
        @ColumnInfo(name = "shirtNumber") var shirtNumber: Int = 0,
        @ColumnInfo(name = "licenseNumber") var licenseNumber: Long = 0L,
        @ColumnInfo(name = "image") var image: String? = null,
        @ColumnInfo(name = "positions") var positions: Int = 0,
        @ColumnInfo(name = "pitching") var pitching: Int = 0,
        @ColumnInfo(name = "batting") var batting: Int = 0,
        @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomPlayer

        if (id != other.id) return false
        if (teamId != other.teamId) return false
        if (name != other.name) return false
        if (shirtNumber != other.shirtNumber) return false
        if (licenseNumber != other.licenseNumber) return false
        if (image != other.image) return false
        if (positions != other.positions) return false
        if (pitching != other.pitching) return false
        if (batting != other.batting) return false
        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + shirtNumber
        result = 31 * result + licenseNumber.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + positions
        result = 31 * result + pitching
        result = 31 * result + batting
        result = 31 * result + (hash?.hashCode() ?: 0)
        return result
    }

}

internal fun RoomPlayer.init(player: Player): RoomPlayer {
    id = player.id
    teamId = player.teamId
    name = player.name
    shirtNumber = player.shirtNumber
    licenseNumber = player.licenseNumber
    image = player.image
    positions = player.positions
    pitching = player.pitching
    batting = player.batting
    hash = player.hash
    return this
}

internal fun RoomPlayer.toPlayer(): Player {
    return Player(id, teamId, name, shirtNumber,licenseNumber, image, positions, pitching, batting, hash)
}