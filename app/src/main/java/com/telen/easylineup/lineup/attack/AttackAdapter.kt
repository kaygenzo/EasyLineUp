package com.telen.easylineup.lineup.attack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.usecases.BatterState
import com.telen.easylineup.views.LineupTypeface
import com.telen.easylineup.views.PlayerPositionFilterView
import com.telen.easylineup.views.PreferencesStyledTextView
import timber.log.Timber

interface OnItemTouchedListener {
    fun onMoved(fromPosition: Int, toPosition: Int)
    fun onSwiped(position: Int)
    fun onDragStart()
    fun onIdle()
}

interface OnDataChangedListener {
    fun onOrderChanged()
}

class BattingOrderAdapter(private val players: MutableList<BatterState>,
                          private val dataListener: OnDataChangedListener?,
                          var teamType: Int,
                          private val lineupTypeface: LineupTypeface
): RecyclerView.Adapter<BattingOrderAdapter.BatterViewHolder>(), OnItemTouchedListener {

    private var positionDescriptions: Array<String>? = null
    var lineupMode = MODE_DISABLED

    override fun onDragStart() {

    }

    override fun onIdle() {
        dataListener?.onOrderChanged()
    }

    override fun onSwiped(position: Int) {

    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val canMoveFrom = if(lineupMode == MODE_DISABLED)
            FieldPosition.isDefensePlayer(players[fromPosition].playerPosition.getPosition())
        else
            FieldPosition.canBeBatterWhenModeEnabled(players[fromPosition].playerPosition.getPosition(), players[fromPosition].playerFlag)
        val canMoveTo = if(lineupMode == MODE_DISABLED)
            FieldPosition.isDefensePlayer(players[toPosition].playerPosition.getPosition())
        else
            FieldPosition.canBeBatterWhenModeEnabled(players[toPosition].playerPosition.getPosition(), players[toPosition].playerFlag)
        if(canMoveFrom && canMoveTo) {
            val fromOrder = players[fromPosition].playerOrder
            val toOrder = players[toPosition].playerOrder
            Timber.d("""Before: (${players[fromPosition].playerName}, ${players[fromPosition].playerOrder}) (${players[toPosition].playerName}, ${players[toPosition].playerOrder})""")
            players[fromPosition].playerOrder = toOrder
            players[toPosition].playerOrder = fromOrder
            Timber.d("""After: (${players[fromPosition].playerName}, ${players[fromPosition].playerOrder}) (${players[toPosition].playerName}, ${players[toPosition].playerOrder})""")
            players.sortBy { it.playerOrder }
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    class BatterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val playerName = view.findViewById<PreferencesStyledTextView>(R.id.playerName)
        val shirtNumber = view.findViewById<PreferencesStyledTextView>(R.id.shirtNumber)
        val fieldPosition = view.findViewById<PreferencesStyledTextView>(R.id.fieldPosition)
        val order = view.findViewById<TextView>(R.id.order)
        val reorderImage = view.findViewById<ImageView>(R.id.reorderImage)
        val positionDesc = view.findViewById<PlayerPositionFilterView>(R.id.fieldPositionDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatterViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.item_player_attack, parent, false)
        return BatterViewHolder(viewItem)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onBindViewHolder(holder: BatterViewHolder, position: Int) {
        val batter = players[position]

        with(holder) {
            playerName.setTypeface(lineupTypeface)
            playerName.text = batter.playerName.trim()

            fieldPosition.setTypeface(lineupTypeface)
            fieldPosition.text = batter.playerPosition.getPosition().toString()

            shirtNumber.setTypeface(lineupTypeface)
            shirtNumber.text = batter.playerNumber

            order.text = batter.playerOrder.toString()

            positionDesc?.apply {
                setBackground(R.drawable.position_unselected_background)
                setTextColor(R.color.tile_team_size_background_color)
                positionDesc.setText(batter.playerPositionDesc)
            }

            order.visibility = if(batter.canShowOrder) View.VISIBLE else View.GONE
            fieldPosition.visibility = if(batter.canShowPosition) View.VISIBLE else View.GONE
            positionDesc.visibility = if(batter.canShowDescription) View.VISIBLE else View.GONE
            reorderImage.visibility = if(batter.canMove) View.VISIBLE else View.GONE
        }
    }
}