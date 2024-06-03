@file:Suppress("DEPRECATION")

package com.zeusinstitute.upiapp


import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.zeusinstitute.upiapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton



//private lateinit var binding: ActivityLoginBinding


class Login : Fragment() {
    @Override
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated(view, savedInstanceState)
        // Access the views here
        val fab = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener  {
            // Do something when button is clicked
            //activity?.setContentView(R.layout.activity_main)
            Log.d("Login", "Button clicked!")
            findNavController().navigate(R.id.login_home)
        }
    }
}
