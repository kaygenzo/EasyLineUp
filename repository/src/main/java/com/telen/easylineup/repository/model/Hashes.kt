/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import java.util.UUID

/**
 * The hash a row is inserted with: the one it already carries, or a new random one if it has
 * none. Rows are matched on it across devices on import, so it is only ever made here, when a
 * row is first written, never as an entity default that a read or an update would also get.
 * Each `toNewRoom()` below is the `toRoom()` an insert uses.
 */
internal fun String?.orNewHash(): String =
    takeUnless { it.isNullOrBlank() } ?: UUID.randomUUID().toString()

internal fun Team.toNewRoom(): RoomTeam = toRoom().copy(hash = hash.orNewHash())

internal fun Player.toNewRoom(): RoomPlayer = toRoom().copy(hash = hash.orNewHash())

internal fun Tournament.toNewRoom(): RoomTournament = toRoom().copy(hash = hash.orNewHash())

internal fun Lineup.toNewRoom(): RoomLineup = toRoom().copy(hash = hash.orNewHash())

internal fun PlayerFieldPosition.toNewRoom(): RoomPlayerFieldPosition =
    toRoom().copy(hash = hash.orNewHash())

internal fun PlayerNumberOverlay.toNewRoom(): RoomPlayerNumberOverlay =
    toRoom().copy(hash = hash.orNewHash())
