package com.zeusinstitute.upiapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.zeusinstitute.upiapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 102
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_main) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu() // Invalidate options menu on destination change
        }

        // Start SMS service if enabled
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val smsEnabled = sharedPref.getBoolean("sms_enabled", true)
        if (smsEnabled) {
            startSMSService()
        }

        // Check and request notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isOnFirstFragment = navController.currentDestination?.id == R.id.firstFragment // Check if on FirstFragment
        menu.setGroupVisible(R.id.main_menu_group, isOnFirstFragment) // Show/hide menu items based on fragment
        if (isOnFirstFragment) {
            menu.findItem(R.id.Profile).isVisible = false
            menu.findItem(R.id.Addons).isVisible = false
            menu.findItem(R.id.hw).isVisible = false
            menu.findItem(R.id.SendCard).isVisible = false
            menu.findItem(R.id.SendUPI).isVisible = false
            menu.findItem(R.id.RecCard).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.log_in -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_login)
                true
            }
            R.id.AboutApp -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_aboutApp)
                true
            }
            R.id.SplitBill -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_splitBillFragment)
                true
            }
            R.id.billHistory -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_billHistory)
                true
            }
            R.id.DynUPI -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_dynamicFragment)
                true
            }
            R.id.Update -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_firstFragment_to_Update)
                true
            }
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment_content_main).navigateUp()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun startSMSService() {
        val intent = Intent(this, SMSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 and above
            startForegroundService(this, intent)
        } else {
            // For older Android versions (Jelly Bean and KitKat)
            ContextCompat.startForegroundService(this, intent)
        }
    }

}

