/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.repository.LineupRepository
import io.reactivex.rxjava3.core.Completable

class InsertLineups(
    private val dao: LineupRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineups: List<Lineup>): Completable {
        return dao.insertLineups(lineups).subscribeOn(schedulersProvider.io())
    }
}
