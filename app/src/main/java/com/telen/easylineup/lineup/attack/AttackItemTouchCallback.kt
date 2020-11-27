package com.telen.easylineup.lineup.attack

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import timber.log.Timber

class AttackItemTouchCallback(val adapter: BattingOrderAdapter, var batterSize: Int, var extraHitterSize: Int = 0): ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = when(viewHolder.adapterPosition) {
            in 0 until (batterSize + extraHitterSize) -> {
                val isSubstitute = adapter.players[viewHolder.adapterPosition].playerPosition == FieldPosition.SUBSTITUTE
                var subsBeforeCount =  0
                adapter.players.forEachIndexed { index, batterState ->
                    if(isSubstitute && index < viewHolder.adapterPosition)
                        subsBeforeCount++
                }
                val isFlex = adapter.players[viewHolder.adapterPosition].playerFlag and PlayerFieldPosition.FLAG_FLEX != 0
                if(!isFlex && (!isSubstitute || subsBeforeCount < extraHitterSize)) {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }
                else 0
            }
            else -> 0
        }
        Timber.d("getMovementFlags=$dragFlags position=${viewHolder.adapterPosition} sizeMax=${batterSize + extraHitterSize -1}")
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        adapter.onMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when(actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> adapter.onDragStart()
            ItemTouchHelper.ACTION_STATE_IDLE -> adapter.onIdle()
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
}