/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.annotations.SerializedName
import com.telen.easylineup.domain.model.export.ExportBase

/**
 * Root wire shape of an `.elu` file. Field names/keys here are the file format contract - they
 * must not change without a compatibility plan, independently of [ExportBase] which is free to
 * evolve with the domain's own needs.
 *
 * This is the only type Gson should ever see directly: [SettingsViewModel] serializes into it
 * before writing, [LoginViewModel] deserializes it before handing data to `ImportData`.
 */
internal data class JsonExportBase(
    @SerializedName("teams") val teams: List<JsonTeamExport>
)

internal fun ExportBase.toJson(): JsonExportBase {
    return JsonExportBase(teams = teams.map { it.toJson() })
}

internal fun JsonExportBase.toDomain(): ExportBase {
    return ExportBase(teams = teams.map { it.toDomain() })
}
