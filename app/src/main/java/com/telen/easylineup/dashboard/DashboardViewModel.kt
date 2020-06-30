package com.telen.easylineup.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.DashboardTile
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject

class DashboardViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    fun registerTilesLiveData(): LiveData<List<DashboardTile>> {
        return Transformations.switchMap(domain.observeTeams()) {
            domain.getTiles()
        }
    }

    fun saveTiles(tiles: List<DashboardTile>): Completable {
        return domain.updateTiles(tiles)
    }
}