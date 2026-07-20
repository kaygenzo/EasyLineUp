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
import java.util.UUID

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
    indices = [Index(value = ["name", "licenseNumber", "teamID"])],
    foreignKeys = [
        ForeignKey(
            entity = RoomTeam::class, parentColumns = ["id"], childColumns = ["teamID"],
            onDelete = ForeignKey.CASCADE
        )
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
    @ColumnInfo(name = "email") var email: String? = null,
    @ColumnInfo(name = "phone") var phone: String? = null,
    @ColumnInfo(name = "sex") var sex: Int = 0,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
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
