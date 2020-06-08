package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Tournament
import kotlinx.android.synthetic.main.dialog_create_lineup.view.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*

interface OnActionButtonListener {
    fun onSaveClicked(lineupName: String, tournament: Tournament)
    fun onCancelClicked()
    fun onRosterChangeClicked()
}

class LineupCreationFormView: ConstraintLayout, TextWatcher {

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
    private var actionClickListener: OnActionButtonListener? = null
    private lateinit var calendar: Calendar

    private var fragmentManager: FragmentManager? = null

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_create_lineup, this)

        lineupTitleInput.addTextChangedListener(this)
        tournamentChoiceAutoComplete.addTextChangedListener(this)

        tournamentsNames = mutableListOf()

        calendar = Calendar.getInstance()
        setTournamentDateHeader(calendar.timeInMillis)

//        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
//            calendar.set(Calendar.YEAR, year)
//            calendar.set(Calendar.MONTH, month)
//            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//
//        }

        adapter = ArrayAdapter(context, R.layout.item_auto_completion, tournamentsNames)
        tournamentChoiceAutoComplete.setAdapter(adapter)
        tournamentChoiceAutoComplete.setOnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position).toString()
            val truePosition = tournamentsNames.indexOf(selected)
            val tournament = tournaments[truePosition]
            calendar.timeInMillis = tournament.createdAt
            setTournamentDateHeader(calendar.timeInMillis)
        }

        dateButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(calendar.timeInMillis)
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                calendar.timeInMillis = it
            }
            fragmentManager?.let {
                datePicker.show(it, "blabla")
                setTournamentDateHeader(calendar.timeInMillis)
            }
        }

        rosterExpandableEdit.setOnClickListener {
            actionClickListener?.onRosterChangeClicked()
        }

        save.setOnClickListener {
            actionClickListener?.onSaveClicked(lineupTitleInput.text.toString(), getSelectedTournament())
        }

        cancel.setOnClickListener {
            actionClickListener?.onCancelClicked()
        }
    }

    fun setFragmentManager(fm: FragmentManager) {
        this.fragmentManager = fm
    }

    private fun setTournamentDateHeader(date: Long) {
        val formattedDate = DateFormat.getDateInstance().format(date)
        dateSummary.text = formattedDate
    }

    fun setOnActionClickListener(listener: OnActionButtonListener) {
        this.actionClickListener = listener
    }

    fun setLineupNameError(error: String) {
        lineupTitleInputLayout.error = error
    }

    fun setTournamentNameError(error: String) {
        tournamentTitleInputLayout.error = error
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

    private fun getSelectedTournament(): Tournament {
        val position = tournamentsNames.indexOf(tournamentChoiceAutoComplete.text.toString())
        Timber.d("position = $position")
        return if(position >= 0) {
            tournaments[position].createdAt = calendar.timeInMillis
            tournaments[position]
        }
        else
            Tournament(name = tournamentChoiceAutoComplete.text.toString().trim(), createdAt = calendar.timeInMillis)
    }
}