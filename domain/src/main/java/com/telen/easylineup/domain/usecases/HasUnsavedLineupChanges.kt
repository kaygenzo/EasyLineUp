/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament

/**
 * Whether leaving the new-lineup form would throw away something the user typed or picked, so
 * it must be confirmed first. A blank name and no tournament mean nothing worth keeping yet.
 */
class HasUnsavedLineupChanges {
    operator fun invoke(name: String, tournament: Tournament?): Boolean =
        name.isNotBlank() || tournament != null
}
