/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

import java.io.Serializable

data class PlayerNumberOverlayExport(
    val id: String,
    val playerId: String?,
    val number: Int
) : Serializable
