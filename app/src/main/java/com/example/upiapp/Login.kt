@file:Suppress("DEPRECATION")

package com.example.upiapp


import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.upiapp.databinding.FragmentLoginBinding


private lateinit var binding: FragmentLoginBinding

//private lateinit var binding: ActivityLoginBinding


class Login : Fragment() {
    @Override
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated(view, savedInstanceState)
        // Access the views here
        val button = view.findViewById<Button>(R.id.floatingActionButton)
        button.setOnClickListener {
            // Do something when button is clicked
            activity?.setContentView(R.layout.activity_main)
        }
    }

    }

      //      override fun onCreate(savedInstanceState: Bundle?) {
       //         super.onCreate(savedInstanceState)
                //setContentView(R.layout.activity_login)
                //setContentView(R.layout.activity_login)
                //custom code

                //setContentView(R.layout.fragment_login)
                //       val button = findViewById<Button>(R.id.floatingActionButton)
                //       button.setOnClickListener {
                //           setContentView(R.layout.fragment_first)
                //       }

         //       }

      //      }
//





    // }






