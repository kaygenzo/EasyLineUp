package com.telen.easylineup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.navigation.NavigationView
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.HostInterface
import com.telen.easylineup.team.swap.SwapTeamFragment
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.DrawerHeader
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.nav_drawer_header.view.*
import timber.log.Timber
import java.io.Serializable

class HomeActivity : BaseActivity(), HostInterface {

    private lateinit var viewModel: HomeViewModel

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private lateinit var drawerHeader: DrawerHeader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        setupActionBarWithNavController(navController, drawerLayout)
        navigationView.setupWithNavController(navController)

        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewModel.registerTeamUpdates().observe(this, Observer {
            loadTeamData()
        })

        drawerHeader = DrawerHeader(this)

        navigationView.addHeaderView(drawerHeader)

        drawerHeader.setOnImageClickListener(View.OnClickListener {
            viewModel.getTeamsCount()
        })

        drawerHeader.setOnSwapTeamClickListener(View.OnClickListener {
            showSwapDialog()
        })

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                val disposable = viewModel.showNewSwapTeamFeature(this@HomeActivity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            showFeatureTargetView(it)
                        }, {
                            Timber.e(it)
                        })
                disposables.add(disposable)
            }
        })

        val disposable = viewModel.observeEvents().subscribe({
            when(it) {
                is GetTeamSuccess -> {
                    drawerHeader.setImage(it.team.image)
                    drawerHeader.setTitle(it.team.name)
                }
                is GetTeamsCountSuccess -> {
                    val argument = Bundle()
                    argument.putInt(Constants.EXTRA_TEAM_COUNT, it.count)
                    navController.navigate(R.id.teamDetailsFragment, argument, NavigationUtils().getOptions())
                    closeDrawer()
                }
                UpdateCurrentTeamSuccess -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    closeDrawer()
                }
                is SwapButtonSuccess -> {
                    val argument = Bundle()
                    argument.putSerializable(Constants.EXTRA_TEAM, it.team as Serializable)
                    val dialog = SwapTeamFragment()
                    dialog.arguments = argument
                    dialog.setHostInterface(this)
                    dialog.show(supportFragmentManager, "SwapTeamFragment")
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })

        disposables.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    private fun showFeatureTargetView(show: Boolean) {
        if(show) {
            FeatureViewFactory.apply(drawerHeader.changeTeam, this, getString(R.string.feature_manage_teams_title),
                    getString(R.string.feature_manage_teams_description), object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    showSwapDialog()
                    view?.dismiss(true)
                }

                override fun onOuterCircleClick(view: TapTargetView?) {
                    view?.dismiss(false)
                }
            })
        }
    }

    private fun showSwapDialog() {
        viewModel.onSwapButtonClicked()
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.findFragmentByTag("SwapTeamFragment")?.let {
            (it as SwapTeamFragment).setHostInterface(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(drawerLayout)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateTeamClick() {
        closeDrawer()
        FirebaseAnalyticsUtils.startTutorial(this, false)
        val intent = Intent(this, TeamCreationActivity::class.java)
        intent.putExtra(Constants.EXTRA_CAN_EXIT, true)
        startActivity(intent)
    }

    override fun onTeamClick(team: Team) {
        viewModel.updateCurrentTeam(team)
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun loadTeamData() {
        viewModel.getTeam()
    }
}
