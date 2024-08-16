@file:Suppress("DEPRECATION")

package com.zeusinstitute.upiapp



import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class Login : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = (activity as? AppCompatActivity)?.supportActionBar?.customView as? Toolbar
        toolbar?.visibility = View.GONE
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val apikey = view.findViewById<TextInputEditText>(R.id.apikey)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        // Access the Toolbar
        val toolbar = (activity as? AppCompatActivity)?.supportActionBar?.customView as? Toolbar

        // Remove the hamburger menu IMMEDIATELY
        toolbar?.navigationIcon = null

        submitButton.setOnClickListener {
            val data = apikey.text.toString()

            // Save data to Shared Preferences
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
            with (sharedPref.edit()) {
                putString("saved_data", data)
                apply()
            }

            // Navigate back to FirstFragment (you might need to adjust this based on your navigation setup)
            findNavController().navigate(R.id.action_login_to_firstFragment)
        }
        return view
    }

}