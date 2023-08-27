package com.telen.easylineup.domain.model.export

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlayerNumberOverlayExport(
    @SerializedName("id") val id: String,
    @SerializedName("playerID") val playerID: String?,
    @SerializedName("number") val number: Int
) : Serializable