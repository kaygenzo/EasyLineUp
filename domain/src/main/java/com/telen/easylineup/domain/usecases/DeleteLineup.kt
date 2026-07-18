/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Completable

class DeleteLineup(
    private val lineupDao: LineupRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineupId: Long?): Completable {
        return (
            lineupId?.let { id ->
                lineupDao.getLineupByIdSingle(id)
                    .flatMapCompletable { lineup -> lineupDao.deleteLineup(lineup) }
            } ?: Completable.error(Exception("Lineup id is null"))
            ).subscribeOn(schedulersProvider.io())
    }
}
