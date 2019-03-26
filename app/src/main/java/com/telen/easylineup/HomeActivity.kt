package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import com.telen.easylineup.battingOrder.BattingOrderFragment
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.team.TeamActivity
import com.telen.easylineup.team.TeamFragment
import kotlinx.android.synthetic.main.activity_list_line_up.*
import kotlinx.android.synthetic.main.app_bar_list_line_up.*
import kotlinx.android.synthetic.main.content_list_line_up.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_line_up)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->

        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

//        val databaseMockProvider = DatabaseMockProvider()
//
//        databaseMockProvider.retrievePlayers().observe(this, Observer { players ->
//            baseballFieldCard.setListPlayer(players)
//        })
//
//        databaseMockProvider.retrieveTeam().observe(this, Observer {team ->
//            Log.d("HomeActivity", "team=$team")
//        })

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BattingOrderFragment())
                .commit()
        
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.list_line_up, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                val intent = Intent(this, TeamActivity::class.java)
                startActivity(intent)
//                supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragmentContainer, TeamFragment())
//                        .commit()
            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_manage -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
