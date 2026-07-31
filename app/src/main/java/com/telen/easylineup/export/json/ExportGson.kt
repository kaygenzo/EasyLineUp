/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.google.gson.Gson

/**
 * Gson instance for reading/writing `.elu` files. It only ever sees [JsonExportBase] and its
 * nested wire types, which carry the file format's field names via `@SerializedName` - the
 * domain export models never touch Gson directly.
 */
internal val exportGson: Gson = Gson()
