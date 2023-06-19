package com.example.upiapp

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //val actionBar: ActionBar? = actionBar
        setSupportActionBar(findViewById(R.id.toolbar2))
    }
}