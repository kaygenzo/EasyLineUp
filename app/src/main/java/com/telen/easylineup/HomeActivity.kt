package com.telen.easylineup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telen.easylineup.data.AppDatabase
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.history.ListLineupFragment
import com.telen.easylineup.newLineup.attack.BattingOrderFragment
import com.telen.easylineup.newLineup.NewLineUpActivity
import com.telen.easylineup.recentActivity.RecentActivityFragment
import com.telen.easylineup.team.TeamFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_line_up.*
import timber.log.Timber

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_line_up)

        DatabaseMockProvider().insertTeam()
                .andThen(DatabaseMockProvider().insertPlayers())
                .andThen(DatabaseMockProvider().insertLineups())
                .andThen(DatabaseMockProvider().insertPlayerFieldPositions())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    DatabaseMockProvider().checkTeam(this)
                    DatabaseMockProvider().checkPlayers(this)
                    DatabaseMockProvider().checkLineups(this)
                    DatabaseMockProvider().checkPlayerFieldPositions(this)
                }, { throwable ->
                    Timber.e(throwable)
                })

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecentActivityFragment())
                .commit()

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
                R.id.navigation_team -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, TeamFragment())
                            .commit()
                    true
                }
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, RecentActivityFragment())
                            .commit()
                    true
                }
                R.id.navigation_lineups -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ListLineupFragment())
                            .commit()
                    true
                }
//                R.id.navigation_settings -> {
//                    val intent = Intent(this@HomeActivity, NewLineUpActivity::class.java)
//                    startActivity(intent)
//                    true
//                }


                else -> false
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.list_line_up, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        when (item.itemId) {
//            R.id.action_settings -> return true
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }
}
