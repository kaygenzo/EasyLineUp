package com.telen.easylineup.repository.model

import androidx.room.*

@Entity(
        tableName = "players",
        indices = [Index(value = ["name", "licenseNumber"])],
        foreignKeys = [
            ForeignKey(entity = Team::class, parentColumns = ["id"], childColumns = ["teamID"],
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class Player(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        @ColumnInfo(name = "teamID") var teamId: Long,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "shirtNumber") var shirtNumber: Int,
        @ColumnInfo(name = "licenseNumber") var licenseNumber: Long,
        @ColumnInfo(name = "image") var image: String? = null
        , @ColumnInfo(name = "positions") var positions: Int = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (id != other.id) return false
        if (teamId != other.teamId) return false
        if (name != other.name) return false
        if (shirtNumber != other.shirtNumber) return false
        if (licenseNumber != other.licenseNumber) return false
        if (image != other.image) return false
        if (positions != other.positions) return false

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
        return result
    }
}