/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.JsonParseException
import com.telen.easylineup.domain.model.export.ExportBase
import java.io.Reader

/** The `.elu` file is not valid JSON, is empty, or misses a field the import cannot do without. */
internal class InvalidExportFileException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Reads an `.elu` file into the domain's [ExportBase], or throws [InvalidExportFileException].
 *
 * Gson ignores Kotlin nullability: a field missing from the file, or set to `null`, comes out as
 * `null` even in a non-null `String` or `List`, and only blows up later, far from the read. So
 * the wire types declare every reference field nullable, and the mapping to the domain checks
 * the ones an import needs (ids, which identify rows, and names) and treats a missing list as
 * empty: older files have no `playerNumberOverlays`, for one.
 */
internal fun parseExportFile(reader: Reader): ExportBase {
    val json = try {
        exportGson.fromJson(reader, JsonExportBase::class.java)
    } catch (e: JsonParseException) {
        throw InvalidExportFileException("Not a valid .elu file", e)
    } ?: throw InvalidExportFileException("Empty .elu file")
    return json.toDomain()
}

internal fun required(value: String?, field: String): String {
    if (value.isNullOrBlank()) {
        throw InvalidExportFileException("Missing $field")
    }
    return value
}
