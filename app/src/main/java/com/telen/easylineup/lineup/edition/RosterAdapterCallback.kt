package com.telen.easylineup.lineup.edition

import com.telen.easylineup.domain.model.RosterItem

interface RosterAdapterCallback {
    fun onNumberChanged(number: Int, item: RosterItem)
}