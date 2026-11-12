/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

/**
 * This text as a literal inside a `LIKE ... ESCAPE '\'` pattern: `%` and `_` stop being
 * wildcards, so searching "100%" doesn't also match "1000". Backslash itself is escaped first.
 */
internal fun String.escapeForLike(): String =
    replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
