@file:Suppress("DEPRECATION")

package com.example.upiapp


import android.app.PendingIntent.getActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.upiapp.databinding.FragmentLoginBinding


private lateinit var binding: FragmentLoginBinding

//private lateinit var binding: ActivityLoginBinding


class Login : AppCompatActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        //setContentView(R.layout.activity_login)
        //custom code

        setContentView(R.layout.fragment_login)
        val actionbar = supportActionBar
        actionbar!!.title = resources.getString(R.string.app_name)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_in -> {
                setContentView(R.layout.fragment_login)
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
    override fun onSupportNavigateUp(): Boolean {
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        //onBackPressed()
        return super.onSupportNavigateUp()
        finish()
    }




    // }


}



