/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class PlayerExport(
    val id: String,
    val name: String,
    val image: String?,
    val shirtNumber: Int,
    val licenseNumber: String,
    val positions: Int,
    val pitching: Int,
    val batting: Int,
    val email: String?,
    val phone: String?,
    val sex: Int
)
