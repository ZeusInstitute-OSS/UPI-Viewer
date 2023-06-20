package com.example.upiapp

import android.app.FragmentTransaction
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.upiapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        binding.fab.setOnClickListener {
                view ->
            Snackbar.make(view, "Profile Mode is WIP!", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .setAction("Action", null).show()

        }
    }

    fun showUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    fun hideUpButton() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /** Called when the user touches the button  */
    public fun goBack(view: View?) {
        androidx.appcompat.R.id.home
        //supportFragmentManager.popBackStack()
       // return true
        // Do something in response to button click
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle item selection
        return when (item.itemId) {
            R.id.log_in -> {
                setContentView(R.layout.fragment_login2)
                true
            }
            R.id.floatingActionButton -> {
                androidx.appcompat.R.id.home
                //setContentView(R.layout.activity_main)
                true
            }
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


  //  override fun onOptionsItemSelected(item: MenuItem): Boolean {
  //      // Handle action bar item clicks here. The action bar will
  //      // automatically handle clicks on the Home/Up button, so long
  //      // as you specify a parent activity in AndroidManifest.xml.
  //      return when (item.itemId) {
  //         R.id.action_settings -> true
  //          else -> super.onOptionsItemSelected(item)
  //      }
  //  }

 //   override fun onSupportNavigateUp(): Boolean {
 //       val navController = findNavController(R.id.nav_host_fragment_content_main)
 //       return navController.navigateUp(appBarConfiguration)
 //               || super.onSupportNavigateUp()
 //   }
}

