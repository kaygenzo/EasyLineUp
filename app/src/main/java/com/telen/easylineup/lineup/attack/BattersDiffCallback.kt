package com.telen.easylineup.lineup.attack

import androidx.recyclerview.widget.DiffUtil
import com.telen.easylineup.domain.model.BatterState

class BattersDiffCallback(private val mOldBatters: List<BatterState>, private val mNewBatters: List<BatterState>): DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if(oldItemPosition < mOldBatters.size && newItemPosition < mOldBatters.size)
            mOldBatters[oldItemPosition].playerID == mOldBatters[newItemPosition].playerID
        else
            false
    }

    override fun getOldListSize(): Int {
        return mOldBatters.size
    }

    override fun getNewListSize(): Int {
        return mNewBatters.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if(oldItemPosition < mOldBatters.size && newItemPosition < mOldBatters.size)
            mOldBatters[oldItemPosition] == mNewBatters[newItemPosition]
        else
            false
    }
}