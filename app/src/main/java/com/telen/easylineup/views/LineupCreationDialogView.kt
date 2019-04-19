package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.data.Tournament
import kotlinx.android.synthetic.main.choose_configuration_new_lineup.view.*
import timber.log.Timber

interface OnFormReadyListener {
    fun onFormStateChanged(isReady: Boolean)
}

class LineupCreationDialogView: ConstraintLayout, TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        readyStateListener?.onFormStateChanged(!TextUtils.isEmpty(lineupTitle.text) && !TextUtils.isEmpty(tournamentChoiceAutoComplete.text))
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private val tournaments: MutableList<Tournament> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var tournamentsNames: MutableList<String>
    private var readyStateListener: OnFormReadyListener? = null

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.choose_configuration_new_lineup, this)

        lineupTitle.addTextChangedListener(this)
        tournamentChoiceAutoComplete.addTextChangedListener(this)

        tournamentsNames = mutableListOf()
        context?.let {
            adapter = ArrayAdapter(context, R.layout.auto_completion_item, tournamentsNames)
            tournamentChoiceAutoComplete.setAdapter(adapter)
        }
    }

    fun setOnReadyStateListener(listener: OnFormReadyListener) {
        this.readyStateListener = listener
    }

    fun setList(tournaments: List<Tournament>) {
        this.tournaments.clear()
        this.tournaments.addAll(tournaments)

        tournamentsNames.apply {
            clear()
            tournaments.forEach {
                add(it.name)
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun getSelectedTournament(): Tournament {
        val position = tournamentsNames.indexOf(tournamentChoiceAutoComplete.text.toString())
        Timber.d("position = $position")
        return if(position >= 0)
            tournaments[position]
        else
            Tournament(name = tournamentChoiceAutoComplete.text.toString())
    }

    fun getLineupTitle(): String {
        return lineupTitle.text.toString()
    }
}