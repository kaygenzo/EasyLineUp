/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper

import com.getkeepsafe.taptargetview.TapTargetView
import com.github.kaygenzo.bugreporter.api.BugReporter
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentDashboardBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_ID
import com.telen.easylineup.domain.model.tiles.LastPlayerNumberResearchData
import com.telen.easylineup.domain.model.tiles.TileData
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.hideSoftKeyboard
import org.koin.android.ext.android.inject
import timber.log.Timber

import java.text.DateFormat
import java.util.Date

class DashboardFragment : BaseFragment("DashboardFragment"), TileClickListener,
ActionMode.Callback {
    private val viewModel by viewModels<DashboardViewModel>()
    private val tileAdapter = DashboardTileAdapter(this)
    private val itemTouchedCallback = DashboardTileTouchCallback(tileAdapter)
    private val itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
    private var binding: FragmentDashboardBinding? = null
    private val bugReporter by inject<BugReporter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDashboardBinding.inflate(inflater, container, false).apply {
            binding = this
            val columnCount = resources.getInteger(R.integer.dashboard_tile_count)
            val gridLayoutManager = GridLayoutManager(context, columnCount)

            tileRecyclerView.apply {
                layoutManager = gridLayoutManager
                adapter = tileAdapter
            }

            viewModel.registerTilesLiveData().observe(viewLifecycleOwner) {
                tileAdapter.submitList(it)
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onTileClicked(type: Int, data: TileData) {
        when (type) {
            Constants.TYPE_TEAM_SIZE -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_team_size")
                findNavController().navigate(
                    R.id.navigation_team,
                    null,
                    NavigationUtils().getOptions()
                )
            }

            Constants.TYPE_LAST_LINEUP -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_last_lineup")
                val lineupId = data.getData()[KEY_LINEUP_ID] as? Long ?: 0L
                val extras = LineupFragment.getArguments(lineupId).apply {
                    putBoolean(Constants.EXTRA_IS_FROM_SHORTCUT, true)
                }
                findNavController().navigate(
                    R.id.lineupFragmentFixed,
                    extras,
                    NavigationUtils().getOptions()
                )
            }

            else -> Timber.d("Click on that tile is not supported, skip")
        }
    }

    override fun onTileLongClicked(type: Int) {
        (activity as? AppCompatActivity)?.let { activity ->
            FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_reorder")
            if (!tileAdapter.inEditMode) {
                viewModel.actionMode = activity.startSupportActionMode(this)?.apply {
                    title = getString(R.string.dashboard_edit_mode)
                }
            }
        }
    }

    override fun onTileSearchNumberClicked(number: Int) {
        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_search_player")
        hideSoftKeyboard()

        launch(viewModel.getShirtNumberHistory(number), { history ->
            val item = tileAdapter.currentList.firstOrNull {
                it.data is LastPlayerNumberResearchData
            }
            (item?.data as? LastPlayerNumberResearchData)?.setHistory(history)
            val indexOfTile = tileAdapter.currentList.indexOfFirst { it == item }
            tileAdapter.notifyItemChanged(indexOfTile)
        })
    }

    override fun onTileSearchNumberHistoryClicked(history: List<ShirtNumberEntry>) {
        if (history.isEmpty()) {
            return
        }

        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_number_history")

        activity?.run {
            AlertDialog.Builder(this)
                .setItems(history.map {
                    val dateInMillis = it.eventTime.takeIf { it > 0 } ?: run { it.createdAt }
                    val date = DateFormat.getDateInstance().format(Date(dateInMillis))
                    "${it.playerName} | $date | ${it.lineupName}"
                }.toTypedArray(), null)
                .create()
                .show()
        }
    }

    override fun onTileTeamSizeSendButtonClicked() {
        activity?.let { activity ->
            val list: MutableList<CharSequence> = mutableListOf()
            list.addAll(activity.resources.getStringArray(R.array.tile_team_size_send_list_labels))
            DialogFactory.getListDialog(activity, list.toTypedArray()) { dialogInterface, i ->
                when (i) {
                    INDEX_SEND_MESSAGES -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_message")
                        launch(viewModel.getPhones(), {
                            if (it.isEmpty()) {
                                DialogFactory.getErrorDialog(
                                    activity,
                                    R.string.tile_team_size_send_empty_title,
                                    R.string.tile_team_size_send_empty_phones
                                ).show()
                            } else {
                                val contactsBuilder = StringBuilder("smsto:")
                                it.forEach { contactsBuilder.append("$it;") }
                                val smsIntent = Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse(contactsBuilder.toString())
                                )
                                smsIntent.resolveActivity(activity.packageManager)?.let {
                                    startActivity(smsIntent)
                                }
                            }
                        })
                    }

                    INDEX_SEND_EMAILS -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_email")
                        launch(viewModel.getEmails(), {
                            if (it.isEmpty()) {
                                DialogFactory.getErrorDialog(
                                    activity,
                                    R.string.tile_team_size_send_empty_title,
                                    R.string.tile_team_size_send_empty_emails
                                )
                                    .show()
                            } else {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    // only email apps should handle this
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, it.toTypedArray())
                                }
                                intent.resolveActivity(activity.packageManager)?.let {
                                    startActivity(intent)
                                }
                            }
                        })
                    }

                    INDEX_SEND_OTHER -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_other")
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                        }
                        intent.resolveActivity(activity.packageManager)?.let {
                            startActivity(intent)
                        }
                    }
                }
                dialogInterface.dismiss()
            }.show()
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        setActionMode(true)
        itemTouchedHelper.attachToRecyclerView(binding?.tileRecyclerView)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        setActionMode(false)
        itemTouchedHelper.attachToRecyclerView(null)
        launch(viewModel.saveTiles(tileAdapter.currentList), {
            activity?.run {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "Save dashboard success", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        activity?.let { activity ->
            launch(viewModel.showNewReportIssueButtonFeature(), { show ->
                if (show) {
                    (activity.findViewById<Toolbar>(R.id.toolbar))?.let { toolbar ->
                        FeatureViewFactory.apply(toolbar, R.id.action_report_issue,
                            activity as AppCompatActivity,
                            getString(R.string.shake_beta_title),
                            getString(R.string.shake_beta_description),
                            object : TapTargetView.Listener() {
                                override fun onTargetClick(view: TapTargetView?) {
                                    bugReporter.startReport(activity)
                                    view?.dismiss(true)
                                }

                                override fun onOuterCircleClick(view: TapTargetView?) {
                                    view?.dismiss(false)
                                }
                            })
                    }
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_report_issue -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_report_issue")
                activity?.run { bugReporter.startReport(this) }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.actionMode?.finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setActionMode(enable: Boolean) {
        tileAdapter.inEditMode = enable
        tileAdapter.notifyDataSetChanged()
    }
}
