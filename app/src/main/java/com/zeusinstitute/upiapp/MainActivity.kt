package com.zeusinstitute.upiapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.zeusinstitute.upiapp.databinding.ActivityMainBinding

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /** Called when the user touches the button  */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle item selection
        return when (item.itemId) {
            R.id.log_in -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_login)
                true
            }
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment_content_main).navigateUp()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

