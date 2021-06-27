package com.telen.easylineup.dashboard

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.getkeepsafe.taptargetview.TapTargetView
import com.instabug.bug.BugReporting
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.tiles.*
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.android.synthetic.main.home_main_content.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class DashboardFragment: BaseFragment("DashboardFragment"), TileClickListener, ActionMode.Callback {

    private lateinit var dashboardViewModel: DashboardViewModel
    private val tileList = mutableListOf<DashboardTile>()
    private lateinit var dataObserver: Observer<List<DashboardTile>>

    private lateinit var tileAdapter: DashboardTileAdapter
    private lateinit var itemTouchedCallback: DashboardTileTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardViewModel = ViewModelProviders.of(this)[DashboardViewModel::class.java]
        dataObserver = Observer {
            tileList.clear()
            tileList.addAll(it)
            tileAdapter.notifyDataSetChanged()
        }
        tileAdapter = DashboardTileAdapter(tileList, this)
        itemTouchedCallback = DashboardTileTouchCallback(tileAdapter)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
        setHasOptionsMenu(true)

        val disposable = dashboardViewModel.eventHandler.subscribe {
            activity?.let { activity ->
                when(it) {
                    is GetTeamEmailsSuccess -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:") // only email apps should handle this
                            putExtra(Intent.EXTRA_EMAIL, it.emails.toTypedArray())
                        }
                        if (intent.resolveActivity(activity.packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                    is GetTeamPhonesSuccess -> {
                        val contactsBuilder = StringBuilder("smsto:")
                        it.phones.forEach { contactsBuilder.append("$it;") }
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(contactsBuilder.toString()))
                        if (smsIntent.resolveActivity(activity.packageManager) != null) {
                            startActivity(smsIntent)
                        }
                    }
                    TeamEmailsEmpty -> {
                        DialogFactory.getErrorDialog(activity, R.string.tile_team_size_send_empty_title, R.string.tile_team_size_send_empty_emails)
                                .show()
                    }
                    TeamPhonesEmpty -> {
                        DialogFactory.getErrorDialog(activity, R.string.tile_team_size_send_empty_title, R.string.tile_team_size_send_empty_phones)
                                .show()
                    }
                }
            }
        }

        this.disposables.add(disposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val columnCount = resources.getInteger(R.integer.dashboard_tile_count)
        val gridLayoutManager = GridLayoutManager(context, columnCount)

        view.tileRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = tileAdapter
        }

        dashboardViewModel.registerTilesLiveData().observe(viewLifecycleOwner, dataObserver)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.tileRecyclerView?.apply {
            adapter = null
        }
    }

    override fun onTileClicked(type: Int, data: ITileData) {
        when(type) {
            Constants.TYPE_TEAM_SIZE -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_team_size")
                findNavController().navigate(R.id.navigation_team, null, NavigationUtils().getOptions())
            }
            Constants.TYPE_LAST_LINEUP -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_last_lineup")
                val lineupID = data.getData()[KEY_LINEUP_ID] as? Long ?: 0L
                val lineupName = data.getData()[KEY_LINEUP_NAME] as? String ?: ""
                val lineupStrategy = data.getData()[KEY_LINEUP_STRATEGY] as TeamStrategy
                val extraHitters = data.getData()[KEY_LINEUP_EXTRA_HITTERS] as? Int ?: 0
                val extras = LineupFragment.getArguments(lineupID, lineupName, lineupStrategy, extraHitters)
                extras.putBoolean(Constants.EXTRA_IS_FROM_SHORTCUT, true)
                findNavController().navigate(R.id.lineupFragmentFixed, extras, NavigationUtils().getOptions())
            }
            else -> {

            }
        }
    }

    override fun onTileLongClicked(type: Int) {
        activity?.let { activity ->
            FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_reorder")
            val inEditMode = tileAdapter.inEditMode
            if (!inEditMode) {
                actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
                actionMode?.title = getString(R.string.dashboard_edit_mode)
            }
        }
    }

    override fun onTileSearchNumberClicked(number: Int) {

        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_search_player")

        hideSoftKeyboard()

        val disposable = dashboardViewModel.getShirtNumberHistory(number)
                .subscribe({ history ->

                    tileList.find { it.data is LastPlayerNumberResearchData }?.data?.let {
                        (it as? LastPlayerNumberResearchData)?.setHistory(history)
                    }
                    tileAdapter.notifyDataSetChanged()
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    override fun onTileSearchNumberHistoryClicked(history: List<ShirtNumberEntry>) {
        if(history.isEmpty())
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
            DialogFactory.getListDialog(activity, list.toTypedArray(), DialogInterface.OnClickListener { dialogInterface, i ->
                when (i) {
                    INDEX_SEND_MESSAGES -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_message")
                        dashboardViewModel.getPhones()
                    }
                    INDEX_SEND_EMAILS -> {
                        FirebaseAnalyticsUtils.onClick(activity, "click_dashboard_send_email")
                        dashboardViewModel.getEmails()
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
            }).show()
        }
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        tileAdapter.inEditMode = true
        tileAdapter.notifyDataSetChanged()
        itemTouchedHelper.attachToRecyclerView(view?.tileRecyclerView)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        tileAdapter.inEditMode = false
        tileAdapter.notifyDataSetChanged()
        itemTouchedHelper.attachToRecyclerView(null)
        val disposable = dashboardViewModel.saveTiles(tileList)
                .subscribe({
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
            val disposable = dashboardViewModel.showNewReportIssueButtonFeature(activity)
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
                if(BuildConfig.UseBetaTool) {
                    BugReporting.show(BugReporting.ReportType.BUG)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }
}