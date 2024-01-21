/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.navigation.NavigationView
import com.telen.easylineup.databinding.ActivityHomeBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.SwapTeamActions
import com.telen.easylineup.team.swap.SwapTeamFragment
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.DrawerHeader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.Serializable

class HomeActivity : BaseActivity(), SwapTeamActions {
    private val viewModel by viewModels<HomeViewModel>()
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private var binding: ActivityHomeBinding? = null
    private val createTeam =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                navController.popBackStack(R.id.navigation_home, false)
            }
        }
    private lateinit var drawerHeader: DrawerHeader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            setupActionBarWithNavController(navController, drawerLayout)
            navigationView.setupWithNavController(navController)

            viewModel.registerTeamUpdates().observe(this@HomeActivity) {
                loadTeamData()
            }

            drawerHeader = DrawerHeader(this@HomeActivity)

            navigationView.addHeaderView(drawerHeader)

            drawerHeader.setOnItemClickListener {
                viewModel.getTeamsCount()
            }

            drawerHeader.setOnSwapTeamClickListener {
                showSwapDialog()
            }

            drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerOpened(drawerView: View) {
                    val disposable = viewModel.showNewSwapTeamFeature()
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
                when (it) {
                    is GetTeamSuccess -> {
                        drawerHeader.setImage(it.team.image, it.team.name)
                        drawerHeader.setTitle(it.team.name)
                    }

                    is GetTeamsCountSuccess -> {
                        val argument = Bundle()
                        argument.putInt(Constants.EXTRA_TEAM_COUNT, it.count)
                        navController.navigate(
                            R.id.teamDetailsFragment,
                            argument,
                            NavigationUtils().getOptions()
                        )
                        closeDrawer()
                    }

                    UpdateCurrentTeamSuccess -> {
                        navController.popBackStack(R.id.navigation_home, false)
                        closeDrawer()
                    }

                    is SwapButtonSuccess -> {
                        val argument = Bundle()
                        argument.putSerializable(Constants.EXTRA_TEAMS, it.teams as Serializable)
                        SwapTeamFragment().apply {
                            arguments = argument
                            setSwapTeamActionsListener(this@HomeActivity)
                        }.show(supportFragmentManager, "SwapTeamFragment")
                    }

                    else -> {}
                }
            }, {
                Timber.e(it)
            })

            disposables.add(disposable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    private fun showFeatureTargetView(show: Boolean) {
        if (show) {
            binding?.drawerLayout?.let {
                FeatureViewFactory.apply(drawerHeader.binding.changeTeam,
                    this,
                    getString(R.string.feature_manage_teams_title),
                    getString(R.string.feature_manage_teams_description),
                    object : TapTargetView.Listener() {
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
    }

    private fun showSwapDialog() {
        viewModel.onSwapButtonClicked()
    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.findFragmentByTag("SwapTeamFragment")?.let {
            (it as SwapTeamFragment).setSwapTeamActionsListener(this)
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
        createTeam.launch(intent)
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
