@file:Suppress("DEPRECATION")

package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        // Set up the listener for the "Enter" key on the keyboard
        apikey.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitButton.performClick() // Trigger the submit button's click listener
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            val data = apikey.text.toString()

            if (isValidUpiId(data)) {
                // Save data to Shared Preferences
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
                with (sharedPref.edit()) {
                    putString("saved_data", data)
                    apply()
                }

                // Navigate back to FirstFragment
                findNavController().navigate(R.id.action_login_to_firstFragment)
            } else {
                // Show error message if UPI ID is invalid
                Toast.makeText(context, "Invalid UPI ID. Please check and try again.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun isValidUpiId(upiId: String): Boolean {
        // UPI ID validation rules
        val upiIdRegex = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")
        return upiId.contains("@") && !upiId.contains(" ") && upiIdRegex.matches(upiId)
    }
}