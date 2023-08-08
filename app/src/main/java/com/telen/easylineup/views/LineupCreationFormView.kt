package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import kotlinx.android.synthetic.main.dialog_create_lineup.view.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*

interface OnActionButtonListener {
    fun onSaveClicked(lineupName: String, tournament: Tournament, lineupEventTime: Long, strategy: TeamStrategy, extraHitters: Int)
    fun onCancelClicked()
    fun onRosterChangeClicked()
}

class LineupCreationFormView: ConstraintLayout, TextWatcher {

    private val tournaments: MutableList<Tournament> = mutableListOf()
    private var strategy: TeamStrategy = TeamStrategy.STANDARD
    private var extraHitters = 0
    private lateinit var tournamentAdapter: ArrayAdapter<String>
    private lateinit var strategyAdapter: ArrayAdapter<String>
    private lateinit var tournamentsNames: MutableList<String>
    private var actionClickListener: OnActionButtonListener? = null
    private lateinit var eventTime: Calendar

    private var fragmentManager: FragmentManager? = null

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_create_lineup, this)

        lineupTitleInput.addTextChangedListener(this)
        tournamentChoiceAutoComplete.addTextChangedListener(this)

        tournamentsNames = mutableListOf()

        eventTime = Calendar.getInstance()

        setTournamentDateHeader(eventTime.timeInMillis)

        tournamentAdapter = ArrayAdapter(context, R.layout.item_auto_completion, tournamentsNames)
        tournamentChoiceAutoComplete.setAdapter(tournamentAdapter)

        dateButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setSelection(eventTime.timeInMillis)
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                eventTime.timeInMillis = it
                setTournamentDateHeader(it)
            }
            fragmentManager?.let {
                datePicker.show(it, "createLineupDatePicker")
            }
        }

        rosterExpandableEdit.setOnClickListener {
            actionClickListener?.onRosterChangeClicked()
        }

        save.setOnClickListener {
            actionClickListener?.onSaveClicked(lineupTitleInput.text.toString(), getSelectedTournament(), eventTime.timeInMillis, strategy, extraHitters)
        }

        cancel.setOnClickListener {
            actionClickListener?.onCancelClicked()
        }

        lineupExtraHittersSpinner.setSelection(extraHitters)
        lineupExtraHittersSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                extraHitters = resources.getIntArray(R.array.extra_hitters_values)[position]
            }
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

    fun setTeamType(teamType: TeamType) {
        val strategies = teamType.getStrategies()
        val strategiesName = when(teamType) {
            TeamType.BASEBALL -> resources.getStringArray(R.array.baseball_strategy_array)
            TeamType.SOFTBALL -> resources.getStringArray(R.array.softball_strategy_array)
            else -> {
                strategy = teamType.defaultStrategy
                return
            }
        }
        strategyAdapter = ArrayAdapter(context, R.layout.item_team_strategy, strategiesName)
        lineupStrategySpinner.apply {
            adapter = strategyAdapter
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    strategy = strategies[position]
                }
            }
        }
        lineupStrategyContainer.visibility = View.VISIBLE
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
        tournamentAdapter.notifyDataSetChanged()
    }

    private fun getSelectedTournament(): Tournament {
        val position = tournamentsNames.indexOf(tournamentChoiceAutoComplete.text.toString())
        Timber.d("position = $position")
        return if(position >= 0) {
//            tournaments[position].createdAt = calendar.timeInMillis
            tournaments[position]
        }
        else
            Tournament(name = tournamentChoiceAutoComplete.text.toString().trim(), createdAt = Calendar.getInstance().timeInMillis)
    }

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
}