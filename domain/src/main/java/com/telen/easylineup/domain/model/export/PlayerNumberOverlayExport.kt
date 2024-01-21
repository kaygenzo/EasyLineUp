/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @property id
 * @property playerId
 * @property number
 */
data class PlayerNumberOverlayExport(
    @SerializedName("id") val id: String,
    @SerializedName("playerID") val playerId: String?,
    @SerializedName("number") val number: Int
) : Serializable
