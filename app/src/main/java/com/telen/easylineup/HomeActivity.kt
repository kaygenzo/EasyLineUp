package com.telen.easylineup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.HostInterface
import com.telen.easylineup.team.swap.SwapTeamFragment
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.DrawerHeader
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.nav_drawer_header.view.*
import timber.log.Timber
import java.io.Serializable

class HomeActivity : AppCompatActivity(), HostInterface {

    private lateinit var viewModel: HomeViewModel
    private var swapDialogDisposable: Disposable? = null

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
            viewModel.getTeam().subscribe({
                navController.navigate(R.id.teamDetailsFragment, null, NavigationUtils().getOptions())
                closeDrawer()
            }, {
                Timber.e(it)
            })
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
            }
        })
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
        swapDialogDisposable?.dispose()
        swapDialogDisposable = viewModel.onSwapButtonClicked().subscribe({
            val argument = Bundle()
            argument.putSerializable(Constants.EXTRA_TEAM, it as Serializable)
            val dialog = SwapTeamFragment()
            dialog.arguments = argument
            dialog.setHostInterface(this)
            dialog.show(supportFragmentManager, "SwapTeamFragment")
        }, {
            Timber.e(it)
        })
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
        val intent = Intent(this, TeamCreationActivity::class.java)
        intent.putExtra(Constants.EXTRA_CAN_EXIT, true)
        startActivity(intent)
    }

    override fun onTeamClick(team: Team) {
        val disposable = viewModel.updateCurrentTeam(team).subscribe({
            navController.popBackStack(R.id.navigation_home, false)
            closeDrawer()
        }, {
            Timber.e(it)
        })
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun loadTeamData() {
        val disposable = viewModel.getTeam().subscribe({
            drawerHeader.setImage(it.image)
            drawerHeader.setTitle(it.name)
        }, { throwable ->
            Timber.d(throwable)
        })
    }
}
