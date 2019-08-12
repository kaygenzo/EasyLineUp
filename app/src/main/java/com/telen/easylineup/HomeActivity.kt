package com.telen.easylineup

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.telen.easylineup.mock.DatabaseMockProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        DatabaseMockProvider().insertTeam()
//                .andThen(
                        DatabaseMockProvider().insertTournaments()
//                )
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
                    DatabaseMockProvider().checkTournaments(this)
                }, { throwable ->
                    Timber.e(throwable)
                })

        val navController = findNavController(R.id.nav_host_fragment)
        bottom_navigation.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
//                || super.onOptionsItemSelected(item)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(requestCode == LineupCreationDialog.REQUEST_CODE_NEW_LINEUP) {
//            if(resultCode == Activity.RESULT_OK) {
//                Snackbar.make(rootView, R.string.lineup_saved, Snackbar.LENGTH_LONG).show()
//            }
//            else {
//                Snackbar.make(rootView, R.string.problem_occurred, Snackbar.LENGTH_LONG).show()
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
}
