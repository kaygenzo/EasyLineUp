package com.telen.easylineup.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.TeamStrategy

class ItemDecoratorAttackRecycler(context: Context?, orientation: Int, batterSize: Int = TeamStrategy.STANDARD.batterSize, extraHitterSize: Int = 0) : DividerItemDecoration(context, orientation) {

    private val mExtraDividerHeight: Int = 15
    private val mExtraDividerIndex = (batterSize + extraHitterSize) - 1

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        if (parent.layoutManager == null || drawable == null) {
            return
        }

        val bounds = Rect()

        canvas.save()
        val left: Int
        val right: Int
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0 until childCount) {

            val child = parent.getChildAt(i)
            val adapterPosition = parent.getChildAdapterPosition(child)

            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + Math.round(child.translationY)
            drawable?.let {
                val top = when (adapterPosition) {
                    mExtraDividerIndex -> bottom - it.intrinsicHeight - mExtraDividerHeight
                    else -> bottom - it.intrinsicHeight
                }
                it.setBounds(left, top, right, bottom)
                it.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        drawable?.let {
            val position = parent.getChildAdapterPosition(view)
            if(position == mExtraDividerIndex)
                outRect.set(0, 0, 0, it.intrinsicHeight + mExtraDividerHeight)
            else
                outRect.set(0, 0, 0, it.intrinsicHeight)
        } ?: outRect.set(0, 0, 0, 0)
    }
}