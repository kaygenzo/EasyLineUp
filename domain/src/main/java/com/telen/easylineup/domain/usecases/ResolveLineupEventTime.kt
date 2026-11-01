/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament

/**
 * Picks the event time to prefill when a tournament is (re)selected for a lineup: the
 * currently chosen time if it already falls within the tournament's start/end window,
 * the tournament's start time if it falls outside it, or now if the tournament has no
 * window at all.
 */
class ResolveLineupEventTime {
    operator fun invoke(tournament: Tournament, currentEventTime: Long): Long {
        return if (tournament.startTime > 0 && tournament.endTime > 0) {
            val outsideRange = tournament.startTime > currentEventTime ||
                currentEventTime > tournament.endTime
            if (outsideRange) tournament.startTime else currentEventTime
        } else {
            System.currentTimeMillis()
        }
    }
}
