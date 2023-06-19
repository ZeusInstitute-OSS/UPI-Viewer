package com.example.upiapp


import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction


//private lateinit var binding: ActivityLoginBinding
class Login : AppCompatActivity() {
    fun openMainActivity() {
        //val intent = Intent(this, MainActivity::class.java)
        setContentView(R.layout.fragment_first)
        //startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val toolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        setSupportActionBar(toolbar)


        val display = supportActionBar
        display?.title="Log into Paytm API"
        display?.setDisplayHomeAsUpEnabled(true)

// Display menu item's title by using a Toast.
fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when(item.itemId){
        android.R.id.home -> {
            finish()
            true

        }

            else ->return super.onOptionsItemSelected(item)
        }

        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        setContentView(R.layout.activity_login)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Log In"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        //OnBackPressedDispatcher()
        return true
    }



}

        //val actionBar: ActionBar? = actionBar
        //setSupportActionBar(findViewById(R.id.toolbar2))
        //actionBar.InflateMenu(R.menu.menu_main);
        //NavUtils.navigateUpFromSameTask(MainActivity.this);
        //val mToolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        //mToolbar.setNavigationIcon(R.drawable.back_foreground);

       // mToolbar.setNavigationOnClickListener() {
       //     setContentView(R.layout.activity_main)
       //     finish();
       //     super.onCreate(savedInstanceState)
       //     binding = ActivityLoginBinding.inflate(layoutInflater)
       //     setContentView(binding.root)

       //     binding.toolbar2.setOnClickListener {
       //         openMainActivity()
       //    }
       // }


