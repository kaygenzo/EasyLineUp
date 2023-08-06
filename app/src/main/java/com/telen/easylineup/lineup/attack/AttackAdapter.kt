package com.telen.easylineup.lineup.attack

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.views.LineupTypeface
import com.telen.easylineup.views.PlayerPositionFilterView
import com.telen.easylineup.views.PreferencesStyledTextView
import timber.log.Timber
import java.util.*

interface OnItemTouchedListener {
    fun onMoved(fromPosition: Int, toPosition: Int)
    fun onSwiped(position: Int)
    fun onDragStart()
    fun onIdle()
}

class BattingOrderAdapter(
    val players: MutableList<BatterState>,
    var lineupTypeface: LineupTypeface = LineupTypeface.NORMAL,
    var lineupMode: Int = MODE_DISABLED,
    var itemTouchHelper: ItemTouchHelper? = null
) : RecyclerView.Adapter<BattingOrderAdapter.BatterViewHolder>(), OnItemTouchedListener {

    override fun onDragStart() {}
    override fun onSwiped(position: Int) {}
    override fun onIdle() {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {
        Timber.d(
            "Adapter: Try to move from position " +
                    "$fromPosition to position $toPosition from a list of ${players.size} elements"
        )
        val fromBatter = players[fromPosition]
        val toBatter = players[toPosition]

        Timber.d("Adapter: canMove from=${fromBatter.canMove} canMoveTo=${toBatter.canMove}")
        if (fromBatter.canMove && toBatter.canMove) {

            //we simply just swap orders

            val fromOrder = fromBatter.playerOrder
            val toOrder = toBatter.playerOrder

            fromBatter.origin.order = toOrder
            toBatter.origin.order = fromOrder
            fromBatter.playerOrder = toOrder
            toBatter.playerOrder = fromOrder

            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(players, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(players, i, i - 1)
                }
            }

            notifyItemChanged(fromPosition)
            notifyItemChanged(toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    class BatterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerName = view.findViewById<PreferencesStyledTextView>(R.id.playerName)
        val shirtNumber = view.findViewById<PreferencesStyledTextView>(R.id.shirtNumber)
        val fieldPosition = view.findViewById<PreferencesStyledTextView>(R.id.fieldPosition)
        val order = view.findViewById<TextView>(R.id.order)
        val reorderImage = view.findViewById<ImageView>(R.id.reorderImage)
        val positionDesc =
            view.findViewById<PlayerPositionFilterView>(R.id.fieldPositionDescription)
        val itemPlayerAttack = view.findViewById<ConstraintLayout>(R.id.itemPlayerAttack)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatterViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_attack, parent, false)
        return BatterViewHolder(viewItem)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun getItemId(position: Int): Long {
        return players[position].playerID
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BatterViewHolder, position: Int) {
        val batter = players[position]

        with(holder) {
            playerName.setTypeface(lineupTypeface)
            playerName.text = batter.playerName.trim()

            fieldPosition.setTypeface(lineupTypeface)
            fieldPosition.text = if (batter.canShowPosition) {
                batter.playerPosition.position.toString()
            } else {
                ""
            }

            shirtNumber.setTypeface(lineupTypeface)
            shirtNumber.text = batter.playerNumber

            order.text = batter.playerOrder.toString()

            positionDesc?.apply {
                setBackground(R.drawable.position_unselected_background)
                setTextColor(R.color.tile_team_size_background_color)
                positionDesc.setText(batter.playerPositionDesc)
            }

            order.visibility = if (batter.canShowOrder) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            positionDesc.visibility = if (batter.canShowDescription) {
                View.VISIBLE
            } else {
                View.GONE
            }
            reorderImage.visibility = if (batter.canMove) {
                View.VISIBLE
            } else {
                if (batter.isEditable) {
                    View.INVISIBLE
                } else {
                    View.GONE
                }
            }
            if (batter.applyBackground) {
                itemPlayerAttack.setBackgroundResource(R.color.lineup_batter_list_highlight)
            } else {
                itemPlayerAttack.setBackgroundResource(0)
            }
            reorderImage.setOnTouchListener { view, motionEvent ->
                itemTouchHelper?.startDrag(this)
                true
            }
        }
    }
}