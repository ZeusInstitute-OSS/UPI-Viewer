@file:Suppress("DEPRECATION")

package com.zeusinstitute.upiapp



import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class Login : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val apikey = view.findViewById<TextInputEditText>(R.id.apikey)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val data = apikey.text.toString()

            // Save data to Shared Preferences
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            with (sharedPref.edit()) {
                putString("saved_data", data)
                apply()
            }

            // Navigate back to FirstFragment (you might need to adjust this based on your navigation setup)
            requireActivity().supportFragmentManager.popBackStack()
        }
        return view
    }
}


