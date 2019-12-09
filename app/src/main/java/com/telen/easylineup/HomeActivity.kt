package com.telen.easylineup

import android.content.Intent
import android.content.SharedPreferences
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
import com.google.android.material.navigation.NavigationView
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.team.swap.HostInterface
import com.telen.easylineup.team.swap.SwapTeamFragment
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.DrawerHeader
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber

class HomeActivity : AppCompatActivity(), HostInterface {

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
            viewModel.getTeam().subscribe({
                val arguments = Bundle()
                arguments.putSerializable(Constants.EXTRA_TEAM, it)
                navController.navigate(R.id.teamEditFragment, arguments, NavigationUtils().getOptions())
                closeDrawer()
            }, {
               Timber.e(it)
            })
        })

        drawerHeader.setOnSwapTeamClickListener(View.OnClickListener {
           viewModel.onSwapButtonClicked()
        })

        viewModel.registerTeamsDialog().observe(this, Observer { teams ->
            SwapTeamFragment(teams, this).show(supportFragmentManager, "SwapTeamFragment")
        })

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
