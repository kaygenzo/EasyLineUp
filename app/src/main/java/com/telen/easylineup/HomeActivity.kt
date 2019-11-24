package com.telen.easylineup

import android.os.Bundle
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
import com.telen.easylineup.repository.Constants
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.DrawerHeader

class HomeActivity : AppCompatActivity() {

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
            drawerHeader.setImage(it.image)
            drawerHeader.setTitle(it.name)
            viewModel.teamID = it.id
        })

        drawerHeader = DrawerHeader(this)

        navigationView.addHeaderView(drawerHeader)

        drawerHeader.setOnClickListener {
            viewModel.teamID?.let {
                val arguments = Bundle()
                arguments.putLong(Constants.TEAM_ID, it)
                navController.navigate(R.id.teamEditFragment, arguments, NavigationUtils().getOptions())
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
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
}
