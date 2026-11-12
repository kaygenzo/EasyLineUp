/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.Player

/**
 * @property id
 * @property teamId
 * @property name
 * @property shirtNumber
 * @property licenseNumber
 * @property image
 * @property positions
 * @property pitching
 * @property batting
 * @property email
 * @property phone
 * @property sex
 * @property hash
 */
@Entity(
    tableName = "players",
    indices = [
        Index(value = ["name", "licenseNumber", "teamID"]),
        Index(value = ["teamID"]),
        Index(value = ["hash"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = RoomTeam::class, parentColumns = ["id"], childColumns = ["teamID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class RoomPlayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "teamID") val teamId: Long = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "shirtNumber") val shirtNumber: Int = 0,
    @ColumnInfo(name = "licenseNumber") val licenseNumber: String = "",
    @ColumnInfo(name = "image") val image: String? = null,
    @ColumnInfo(name = "positions") val positions: Int = 0,
    @ColumnInfo(name = "pitching") val pitching: Int = 0,
    @ColumnInfo(name = "batting") val batting: Int = 0,
    @ColumnInfo(name = "email") val email: String? = null,
    @ColumnInfo(name = "phone") val phone: String? = null,
    @ColumnInfo(name = "sex") val sex: Int = 0,
    @ColumnInfo(name = "hash") val hash: String? = null
)

internal fun Player.toRoom(): RoomPlayer {
    return RoomPlayer(
        id = id,
        teamId = teamId,
        name = name,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        image = image,
        positions = positions,
        pitching = pitching,
        batting = batting,
        email = email,
        phone = phone,
        hash = hash,
        sex = sex
    )
}

internal fun RoomPlayer.toDomain(): Player {
    return Player(
        id = id,
        teamId = teamId,
        name = name,
        shirtNumber = shirtNumber,
        licenseNumber = licenseNumber,
        image = image,
        positions = positions,
        pitching = pitching,
        batting = batting,
        email = email,
        phone = phone,
        sex = sex,
        hash = hash
    )
}
