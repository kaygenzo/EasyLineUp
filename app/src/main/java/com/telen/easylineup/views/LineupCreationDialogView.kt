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
import com.telen.easylineup.repository.data.Tournament
import kotlinx.android.synthetic.main.dialog_create_lineup.view.*
import timber.log.Timber
import java.util.*

interface OnFormReadyListener {
    fun onFormStateChanged(isReady: Boolean)
}

interface OnActionButtonListener {
    fun onSaveClicked()
    fun onCancelClicked()
}

class LineupCreationDialogView: ConstraintLayout, TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        //readyStateListener?.onFormStateChanged()
        val lineupName = lineupTitleInput.text
        val tournamentName = tournamentChoiceAutoComplete.text
        if(!TextUtils.isEmpty(lineupName?.trim())) {
            lineupTitleInputLayout.error = null
        }
        if(!TextUtils.isEmpty(tournamentName.trim())){
            tournamentTitleInputLayout.error = null
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private val tournaments: MutableList<Tournament> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var tournamentsNames: MutableList<String>
    private var readyStateListener: OnFormReadyListener? = null
    private var actionClickListener: OnActionButtonListener? = null
    private lateinit var calendar: Calendar

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_create_lineup, this)

        lineupTitleInput.addTextChangedListener(this)
        tournamentChoiceAutoComplete.addTextChangedListener(this)

        tournamentsNames = mutableListOf()

        calendar = Calendar.getInstance()

        calendarView.setDate(calendar.timeInMillis, true, true)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        adapter = ArrayAdapter(context, R.layout.item_auto_completion, tournamentsNames)
        tournamentChoiceAutoComplete.setAdapter(adapter)
        tournamentChoiceAutoComplete.setOnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position).toString()
            val truePosition = tournamentsNames.indexOf(selected)
            val tournament = tournaments[truePosition]
            calendar.timeInMillis = tournament.createdAt
            calendarView.setDate(calendar.timeInMillis, true, true)
        }

        expandableButton.setOnClickListener {
            when(expandableView.isExpanded) {
                true -> {
                    expandableView.collapse()
                    expandableArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                }
                else -> {
                    expandableView.expand()
                    expandableArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                }
            }
        }

        save.setOnClickListener {
            val lineupName = lineupTitleInput.text
            val tournamentName = tournamentChoiceAutoComplete.text
            if(!TextUtils.isEmpty(lineupName?.trim()) && !TextUtils.isEmpty(tournamentName.trim()))
                actionClickListener?.onSaveClicked()
            else if(TextUtils.isEmpty(lineupName?.trim())) {
                lineupTitleInputLayout.error = resources.getString(R.string.lineup_creation_error_name_empty)
            }
            else {
                tournamentTitleInputLayout.error = resources.getString(R.string.lineup_creation_error_tournament_empty)
            }
        }

        cancel.setOnClickListener {
            actionClickListener?.onCancelClicked()
        }
    }

    fun setOnActionClickListener(listener: OnActionButtonListener) {
        this.actionClickListener = listener
    }

    fun setOnFormReadyListener(listener: OnFormReadyListener) {
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
        return if(position >= 0) {
            tournaments[position].createdAt = calendar.timeInMillis
            tournaments[position]
        }
        else
            Tournament(name = tournamentChoiceAutoComplete.text.toString().trim(), createdAt = calendar.timeInMillis)
    }

    fun getLineupTitle(): String {
        return lineupTitleInput.text.toString().trim()
    }

    fun setSaveButtonEnabled(enabled: Boolean) {
        save.isEnabled = enabled
    }
}