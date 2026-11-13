/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.hasDateWindow
import com.telen.easylineup.domain.ports.Clock
import java.util.Calendar
import java.util.TimeZone

/**
 * Picks the event time to prefill when a tournament is (re)selected for a lineup: the
 * currently chosen time if it already falls within the tournament's start/end window,
 * the tournament's start time if it falls outside it, or now if the tournament has no
 * window at all.
 *
 * The window covers whole days: a tournament's dates are stored as the local midnight of its
 * first and last day, so a game at 3 pm on the last day is still inside it.
 */
class ResolveLineupEventTime(
    private val clock: Clock,
    private val timeZone: () -> TimeZone = TimeZone::getDefault
) {
    operator fun invoke(tournament: Tournament, currentEventTime: Long): Long {
        return if (tournament.hasDateWindow()) {
            val outsideRange = currentEventTime < tournament.startTime ||
                currentEventTime >= startOfNextDay(tournament.endTime)
            if (outsideRange) tournament.startTime else currentEventTime
        } else {
            clock.nowMillis()
        }
    }

    /** Midnight after the day [time] falls on, in local time: 23 or 25 hours on a DST change. */
    private fun startOfNextDay(time: Long): Long =
        Calendar.getInstance(timeZone()).apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
}
