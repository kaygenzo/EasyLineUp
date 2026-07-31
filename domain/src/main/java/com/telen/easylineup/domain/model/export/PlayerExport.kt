/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

data class PlayerExport(
    val id: String,
    val name: String,
    var image: String?,
    val shirtNumber: Int,
    val licenseNumber: String,
    val positions: Int,
    val pitching: Int,
    val batting: Int,
    var email: String?,
    var phone: String?,
    var sex: Int
)
