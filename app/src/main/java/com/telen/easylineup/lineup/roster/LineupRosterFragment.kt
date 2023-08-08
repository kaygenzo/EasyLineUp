package com.telen.easylineup.lineup.roster

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.views.RosterEntryView
import kotlinx.android.synthetic.main.fragment_lineup_roster.view.*
import kotlinx.android.synthetic.main.view_actions_form.view.*
import timber.log.Timber

class LineupRosterFragment : BaseFragment("LineupRosterFragment"), RosterAdapterCallback {

    private lateinit var rosterAdapter: RosterAdapter

    private lateinit var viewModel: LineupRosterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(LineupRosterViewModel::class.java)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID) ?: 0
        rosterAdapter = RosterAdapter(viewModel.getRosterItems(), this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_lineup_roster, container, false)

        view.rosterListItems.apply {
            adapter = rosterAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.observeLineup().observe(viewLifecycleOwner, Observer {
            val disposable = viewModel.getCurrentRoster()
                    .subscribe({
                        viewModel.setRosterItems(it)
                        rosterAdapter.notifyDataSetChanged()
                    }, {
                        Timber.e(it)
                    })
            disposables.add(disposable)
        })

        view.saveButton.setOnClickListener {
            val saveDisposable = viewModel.saveOverlays().subscribe({
                findNavController().popBackStack()
            }, {
                Timber.e(it)
            })
            disposables.add(saveDisposable)
        }

        view.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onNumberChanged(number: Int, item: RosterItem) {
        viewModel.numberChanged(number, item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roaster_edition, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.let { activity ->
            this.disposables.add(viewModel.getRoster().subscribe({ roster ->

                val names = mutableListOf<CharSequence>().apply {
                    addAll(roster.map { it.player.name})
                }
                val checked = mutableListOf<Boolean>().apply {
                    addAll(roster.map { it.status })
                }

                DialogFactory.getMultiChoiceDialog(
                        context = activity,
                        title = R.string.roster_list_player_dialog_title,
                        items = names.toTypedArray(),
                        checkedItems = checked.toBooleanArray(),
                        listener = DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
                            roster[which].status = isChecked
                        },
                        confirmClick = DialogInterface.OnClickListener { _, _ ->
                            this.disposables.add(viewModel.saveUpdatedRoster(roster).subscribe({}, {
                               Timber.e(it)
                            }))
                        }
                ).show()

            }, {
                Timber.e(it)
            }))
        }
        return super.onOptionsItemSelected(item)
    }
}

interface RosterAdapterCallback {
    fun onNumberChanged(number: Int, item: RosterItem)
}

class RosterAdapter(private val items: List<RosterItem>, private val listener: RosterAdapterCallback): RecyclerView.Adapter<RosterAdapter.RosterViewHolder>() {

    data class RosterViewHolder(val view: RosterEntryView): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterViewHolder {
        return RosterViewHolder(RosterEntryView(parent.context))
    }

    override fun getItemCount(): Int {
       return items.count()
    }

    override fun onBindViewHolder(holder: RosterViewHolder, position: Int) {
        val item = items[position]
        holder.view.setPlayerName(item.player.name)
        holder.view.setTextListener(null)
        item.playerNumberOverlay?.let {
            holder.view.setNumber(it.number)
        } ?: let {
            holder.view.setNumber(item.player.shirtNumber)
        }
        holder.view.setTextListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    listener.onNumberChanged(s.toString().toInt(), item)
                }
                catch (e: NumberFormatException) {
                    Timber.e(e.message)
                    listener.onNumberChanged(0, item)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }
}