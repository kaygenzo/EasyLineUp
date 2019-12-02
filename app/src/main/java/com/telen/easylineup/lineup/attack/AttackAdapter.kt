package com.telen.easylineup.lineup.attack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.MODE_DH
import com.telen.easylineup.repository.model.MODE_NONE
import com.telen.easylineup.repository.model.PlayerWithPosition
import com.telen.easylineup.views.PlayerPositionFilterView
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

class BattingOrderAdapter(private val players: MutableList<PlayerWithPosition>, val dataListener: OnDataChangedListener?, val isEditable: Boolean): RecyclerView.Adapter<BattingOrderAdapter.BatterViewHolder>(), OnItemTouchedListener {

    private var positionDescriptions: Array<String>? = null
    var lineupMode = MODE_NONE

    override fun onDragStart() {

    }

    override fun onIdle() {
        dataListener?.onOrderChanged()
    }

    override fun onSwiped(position: Int) {

    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val canMoveFrom = if(lineupMode == MODE_NONE)
            FieldPosition.isDefensePlayer(players[fromPosition].position)
        else
            FieldPosition.canBeBatterWhenDH(players[fromPosition].position)
        val canMoveTo = if(lineupMode == MODE_NONE)
            FieldPosition.isDefensePlayer(players[toPosition].position)
        else
            FieldPosition.canBeBatterWhenDH(players[toPosition].position)
        if(canMoveFrom && canMoveTo) {
            val fromOrder = players[fromPosition].order
            val toOrder = players[toPosition].order
            Timber.d("Before: ("+players[fromPosition].playerName+", "+players[fromPosition].order+") ("+players[toPosition].playerName+", "+players[toPosition].order+")")
            players[fromPosition].order = toOrder
            players[toPosition].order = fromOrder
            Timber.d("After: ("+players[fromPosition].playerName+", "+players[fromPosition].order+") ("+players[toPosition].playerName+", "+players[toPosition].order+")")
            players.sortBy { it.order }
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    class BatterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val playerName = view.findViewById<TextView>(R.id.playerName)
        val shirtNumber = view.findViewById<TextView>(R.id.shirtNumber)
        val fieldPosition = view.findViewById<TextView>(R.id.fieldPosition)
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
        val player = players[position]

        if(positionDescriptions == null)
            positionDescriptions = holder.positionDesc.context.resources.getStringArray(R.array.field_positions_list)

        with(holder) {

            val isSubstitute = FieldPosition.isSubstitute(player.position)
            val isDefensePlayer = FieldPosition.isDefensePlayer(player.position)

            playerName.text = player.playerName.trim()
            shirtNumber.text = player.shirtNumber.toString()

            if(isDefensePlayer)
                fieldPosition.text = player.position.toString()
            else
                fieldPosition.text = ""

            order.text = player.order.toString()
            if(BuildConfig.DEBUG)
                order.visibility = View.VISIBLE

            positionDescriptions?.let { descs ->
                FieldPosition.getFieldPosition(player.position)?.let {
                    positionDesc.setText(descs[it.ordinal])
                }
                positionDesc.setBackground(R.drawable.position_unselected_background)
                positionDesc.setTextColor(R.color.tile_team_size_background_color)
            }

            if(!isEditable || isSubstitute || (lineupMode == MODE_DH && FieldPosition.getFieldPosition(player.position) == FieldPosition.PITCHER)) {
                reorderImage.visibility = View.GONE
            }
            else {
                reorderImage.visibility = View.VISIBLE
            }

            if(!isEditable || isSubstitute || (lineupMode == MODE_DH && FieldPosition.getFieldPosition(player.position) == FieldPosition.DH)) {
                positionDesc.visibility = View.VISIBLE
            }
            else {
                positionDesc.visibility = View.GONE
            }
        }
    }
}