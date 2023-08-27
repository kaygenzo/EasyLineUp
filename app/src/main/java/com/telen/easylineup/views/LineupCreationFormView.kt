package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.databinding.DialogCreateLineupBinding
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.utils.DialogFactory
import java.text.DateFormat

interface OnActionButtonListener {
    fun onSaveClicked()
    fun onCancelClicked()
    fun onRosterChangeClicked()
    fun onCreateTournamentClicked()
    fun onTournamentSelected(tournament: Tournament)
    fun onLineupStartTimeChanged(time: Long)
    fun onLineupNameChanged(name: String)
    fun onStrategyChanged(strategy: TeamStrategy)
    fun onExtraHittersChanged(count: Int)
}

class LineupCreationFormView : ConstraintLayout {

    private val tournaments: MutableList<Tournament> = mutableListOf()
    private val tournamentsNames: MutableList<String> = mutableListOf()
    private val tournamentAdapter: ArrayAdapter<String>

    private var actionClickListener: OnActionButtonListener? = null

    val binding: DialogCreateLineupBinding =
        DialogCreateLineupBinding.inflate(LayoutInflater.from(context), this, true)

    private var fragmentManager: FragmentManager? = null
    private var lineupStartTime: Long = System.currentTimeMillis()
    private var selectedTournament: Tournament? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        with(binding) {
            setTournamentDateHeader(lineupStartTime)

            lineupTitleInput.addTextChangedListener {
                actionClickListener?.onLineupNameChanged(it.toString())
                setLineupNameError("")
            }

            tournamentAdapter =
                ArrayAdapter(context, R.layout.item_auto_completion, tournamentsNames)
            tournamentChoiceAutoComplete.setAdapter(tournamentAdapter)
            tournamentChoiceAutoComplete.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    tournaments[position].let {
                        selectedTournament = it
                        actionClickListener?.onTournamentSelected(it)
                        setTournamentNameError("")
                    }
                    updateLineupTime()
                }

            dateSummary.setOnClickListener {
                selectedTournament?.let {
                    showDatePicker(it)
                } ?: let {
                    DialogFactory.getErrorDialog(
                        context,
                        R.string.dialog_error_select_tournament_first_message,
                        0
                    ).show()
                }
            }

            rosterExpandableEdit.setOnClickListener {
                actionClickListener?.onRosterChangeClicked()
            }

            actionContainer.saveClickListener = OnClickListener {
                actionClickListener?.onSaveClicked()
            }

            actionContainer.cancelClickListener = OnClickListener {
                actionClickListener?.onCancelClicked()
            }

            lineupExtraHittersSpinner.setSelection(0)
            lineupExtraHittersSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        id: Long
                    ) {
                        val value = resources.getIntArray(R.array.extra_hitters_values)[position]
                        actionClickListener?.onExtraHittersChanged(value)
                    }
                }
            createTournament.setOnClickListener {
                actionClickListener?.onCreateTournamentClicked()
            }
        }
    }

    private fun updateLineupTime() {
        selectedTournament?.let {
            if (it.startTime > 0 && it.endTime > 0) {
                if (it.startTime > lineupStartTime || lineupStartTime > it.endTime) {
                    lineupStartTime = it.startTime
                    setTournamentDateHeader(lineupStartTime)
                }
            } else {
                lineupStartTime = System.currentTimeMillis()
                setTournamentDateHeader(lineupStartTime)
            }
        }
    }

    private fun showDatePicker(tournament: Tournament) {
        var constraints: CalendarConstraints? = null
        if (tournament.startTime > 0 && tournament.endTime > 0) {
            val min = DateValidatorPointForward.from(tournament.startTime)
            val max = DateValidatorPointBackward.before(tournament.endTime)
            val validators = CompositeDateValidator.allOf(listOf(min, max))
            constraints = CalendarConstraints.Builder().setValidator(validators).build()
        }
        val datePickerBuilder = MaterialDatePicker.Builder
            .datePicker()
            .setSelection(lineupStartTime)
        constraints?.let {
            datePickerBuilder.setCalendarConstraints(it)
        }
        val datePicker = datePickerBuilder.build()

        datePicker.addOnPositiveButtonClickListener {
            lineupStartTime = it
            setTournamentDateHeader(it)
            actionClickListener?.onLineupStartTimeChanged(lineupStartTime)
        }
        fragmentManager?.let {
            datePicker.show(it, "createLineupDatePicker")
        }
    }

    fun setFragmentManager(fm: FragmentManager) {
        this.fragmentManager = fm
    }

    private fun setTournamentDateHeader(date: Long) {
        val formattedDate = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
        binding.dateSummary.text = formattedDate
    }

    fun setOnActionClickListener(listener: OnActionButtonListener) {
        this.actionClickListener = listener.apply {
            onLineupStartTimeChanged(lineupStartTime)
        }
    }

    fun setTeamType(teamType: TeamType) {
        val strategies = teamType.getStrategies()
        val strategiesName = teamType.getStrategiesDisplayName(context) ?: return
        val strategyAdapter = ArrayAdapter(context, R.layout.item_team_strategy, strategiesName)
        binding.lineupStrategySpinner.apply {
            adapter = strategyAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val strategy = strategies[position]
                    actionClickListener?.onStrategyChanged(strategy)
                }
            }
        }
        binding.lineupStrategyContainer.visibility = View.VISIBLE
    }

    fun setLineupNameError(error: String) {
        binding.lineupTitleInputLayout.error = error
    }

    fun setTournamentNameError(error: String) {
        binding.tournamentTitleInputLayout.error = error
    }

    fun setList(tournaments: List<Tournament>) {
        this.tournaments.clear()
        this.tournaments.addAll(tournaments)

        tournamentsNames.apply {
            clear()
            addAll(tournaments.map { it.name })
        }
        tournamentAdapter.notifyDataSetChanged()
        selectTournamentPosition()
    }

    fun selectTournament(tournament: Tournament) {
        this.selectedTournament = tournament
        selectTournamentPosition()
    }

    private fun selectTournamentPosition() {
        selectedTournament?.let { tournament ->
            tournaments.indexOfFirst { it.id == tournament.id }
                .takeIf { it >= 0 }
                ?.let {
                    binding.tournamentChoiceAutoComplete.setText(tournamentsNames[it], false)
                    setTournamentNameError("")
                }
        }
    }
}