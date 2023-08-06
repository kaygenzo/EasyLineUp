package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.R
import com.telen.easylineup.databinding.DialogCreateLineupBinding
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import timber.log.Timber
import java.text.DateFormat
import java.util.Calendar

interface OnActionButtonListener {
    fun onSaveClicked(
        lineupName: String,
        tournament: Tournament,
        lineupEventTime: Long,
        strategy: TeamStrategy,
        extraHitters: Int
    )

    fun onCancelClicked()
    fun onRosterChangeClicked()
}

class LineupCreationFormView : ConstraintLayout, TextWatcher {

    private val tournaments: MutableList<Tournament> = mutableListOf()
    private val tournamentAdapter: ArrayAdapter<String>
    private val tournamentsNames: MutableList<String>

    private var strategy: TeamStrategy = TeamStrategy.STANDARD
    private var extraHitters = 0
    private var actionClickListener: OnActionButtonListener? = null
    private var eventTime: Calendar

    val binding: DialogCreateLineupBinding =
        DialogCreateLineupBinding.inflate(LayoutInflater.from(context), this, true)

    private var fragmentManager: FragmentManager? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        with(binding) {
            lineupTitleInput.addTextChangedListener(this@LineupCreationFormView)
            tournamentChoiceAutoComplete.addTextChangedListener(this@LineupCreationFormView)

            tournamentsNames = mutableListOf()

            eventTime = Calendar.getInstance()

            setTournamentDateHeader(eventTime.timeInMillis)

            tournamentAdapter =
                ArrayAdapter(context, R.layout.item_auto_completion, tournamentsNames)
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

            actionContainer.saveClickListener = OnClickListener {
                actionClickListener?.onSaveClicked(
                    lineupTitleInput.text.toString(),
                    getSelectedTournament(),
                    eventTime.timeInMillis,
                    strategy,
                    extraHitters
                )
            }

            actionContainer.cancelClickListener = OnClickListener {
                actionClickListener?.onCancelClicked()
            }

            lineupExtraHittersSpinner.setSelection(extraHitters)
            lineupExtraHittersSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        id: Long
                    ) {
                        extraHitters = resources.getIntArray(R.array.extra_hitters_values)[position]
                    }
                }
        }
    }

    fun setFragmentManager(fm: FragmentManager) {
        this.fragmentManager = fm
    }

    private fun setTournamentDateHeader(date: Long) {
        val formattedDate = DateFormat.getDateInstance().format(date)
        binding.dateSummary.text = formattedDate
    }

    fun setOnActionClickListener(listener: OnActionButtonListener) {
        this.actionClickListener = listener
    }

    fun setTeamType(teamType: TeamType) {
        val strategies = teamType.getStrategies()
        val strategiesName = when (teamType) {
            TeamType.BASEBALL -> resources.getStringArray(R.array.baseball_strategy_array)
            TeamType.SOFTBALL -> resources.getStringArray(R.array.softball_strategy_array)
            else -> {
                strategy = teamType.defaultStrategy
                return
            }
        }
        val strategyAdapter = ArrayAdapter(context, R.layout.item_team_strategy, strategiesName)
        binding.lineupStrategySpinner.apply {
            adapter = strategyAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    strategy = strategies[position]
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
            tournaments.forEach {
                add(it.name)
            }
        }
        tournamentAdapter.notifyDataSetChanged()
    }

    private fun getSelectedTournament(): Tournament {
        val position =
            tournamentsNames.indexOf(binding.tournamentChoiceAutoComplete.text.toString())
        Timber.d("position = $position")
        return if (position >= 0) {
//            tournaments[position].createdAt = calendar.timeInMillis
            tournaments[position]
        } else
            Tournament(
                name = binding.tournamentChoiceAutoComplete.text.toString().trim(),
                createdAt = Calendar.getInstance().timeInMillis
            )
    }

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            val lineupName = lineupTitleInput.text
            val tournamentName = tournamentChoiceAutoComplete.text
            if (!TextUtils.isEmpty(lineupName?.trim())) {
                lineupTitleInputLayout.error = null
            }
            if (!TextUtils.isEmpty(tournamentName.trim())) {
                tournamentTitleInputLayout.error = null
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}