package com.telen.easylineup.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
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
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.tiles.*
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.home_main_content.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.DateFormat
import java.util.*

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
        activity?.let { bugReporter.listen(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDashboardBinding.inflate(inflater, container, false)
        this.binding = binding
        val columnCount = resources.getInteger(R.integer.dashboard_tile_count)
        val gridLayoutManager = GridLayoutManager(context, columnCount)

        binding.tileRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = tileAdapter
        }

        viewModel.registerTilesLiveData().observe(viewLifecycleOwner) {
            tileAdapter.submitList(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onTileClicked(type: Int, data: ITileData) {
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
                val lineupID = data.getData()[KEY_LINEUP_ID] as? Long ?: 0L
                val lineupName = data.getData()[KEY_LINEUP_NAME] as? String ?: ""
                val lineupStrategy = data.getData()[KEY_LINEUP_STRATEGY] as TeamStrategy
                val extraHitters = data.getData()[KEY_LINEUP_EXTRA_HITTERS] as? Int ?: 0
                val extras =
                    LineupFragment.getArguments(lineupID, lineupName, lineupStrategy, extraHitters)
                extras.putBoolean(Constants.EXTRA_IS_FROM_SHORTCUT, true)
                findNavController().navigate(
                    R.id.lineupFragmentFixed,
                    extras,
                    NavigationUtils().getOptions()
                )
            }
            else -> {
                Timber.d("Click on that tile is not supported, skip")
            }
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

        val disposable = viewModel.getShirtNumberHistory(number).subscribe({ history ->
            val item = tileAdapter.currentList.firstOrNull {
                it.data is LastPlayerNumberResearchData
            }
            (item?.data as? LastPlayerNumberResearchData)?.setHistory(history)
            val indexOfTile = tileAdapter.currentList.indexOfFirst { it == item }
            tileAdapter.notifyItemChanged(indexOfTile)
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)
    }

    override fun onTileSearchNumberHistoryClicked(history: List<ShirtNumberEntry>) {
        if (history.isEmpty())
            return

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
            val list = mutableListOf<CharSequence>()
            list.addAll(activity.resources.getStringArray(R.array.tile_team_size_send_list_labels))
            DialogFactory.getListDialog(activity, list.toTypedArray()) { dialogInterface, i ->
                when (i) {
                    INDEX_SEND_MESSAGES -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_message")
                        disposables.add(viewModel.getPhones().subscribe({
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
                                if (smsIntent.resolveActivity(activity.packageManager) != null) {
                                    startActivity(smsIntent)
                                }
                            }
                        }, {
                            Timber.e(it)
                        }))
                    }
                    INDEX_SEND_EMAILS -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_email")
                        disposables.add(viewModel.getEmails().subscribe({
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
                                if (intent.resolveActivity(activity.packageManager) != null) {
                                    startActivity(intent)
                                }
                            }
                        }, {
                            Timber.e(it)
                        }))
                    }
                    INDEX_SEND_OTHER -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_other")
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                        }
                        if (intent.resolveActivity(activity.packageManager) != null) {
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
        val disposable = viewModel.saveTiles(tileAdapter.currentList).subscribe({
            activity?.run {
                if (BuildConfig.DEBUG)
                    Toast.makeText(this, "Save dashboard success", Toast.LENGTH_SHORT).show()
            }
        }, {
            Timber.e(it)
        })
        this.disposables.add(disposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        activity?.let { activity ->
            val disposable = viewModel.showNewReportIssueButtonFeature(activity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ show ->
                    if (show) {
                        (activity.toolbar as? Toolbar)?.let { toolbar ->
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
                }, {
                    Timber.e(it)
                })
            disposables.add(disposable)
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