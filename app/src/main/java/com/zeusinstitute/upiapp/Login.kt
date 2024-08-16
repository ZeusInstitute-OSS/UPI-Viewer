@file:Suppress("DEPRECATION")

package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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

        val apikeyLayout = view.findViewById<TextInputLayout>(R.id.apikeyLayout)
        val apikey = view.findViewById<TextInputEditText>(R.id.apikey)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        // Initially hide the submitButton
        submitButton.visibility = View.GONE

        // Add TextWatcher to apikey
        apikey.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                submitButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Access the Toolbar
        val toolbar = (activity as? AppCompatActivity)?.supportActionBar?.customView as? Toolbar

        // Remove the hamburger menu IMMEDIATELY
        toolbar?.navigationIcon = null

        // Set up the listener for the "Enter" key on the keyboard
        apikey.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitButton.performClick()

                // Hide Keyboard
                val imm =
                    ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(apikey.windowToken, 0)

                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            val data = apikey.text.toString()

            val invalidReasons = getInvalidUpiIdReasons(data) // Get multiple reasons
            if (invalidReasons.isEmpty()) {
                // Save data to Shared Preferences
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return@setOnClickListener
                with (sharedPref.edit()) {
                    putString("saved_data", data)
                    apply()
                }

                // Navigate back to FirstFragment
                findNavController().navigate(R.id.action_login_to_firstFragment)
            } else {
                // Show error message directly in the TextInputLayout with all reasons
                apikeyLayout.error = "Invalid UPI ID:\n${invalidReasons.joinToString("\n")}"
            }
        }
        return view
    }

    private fun isValidUpiId(upiId: String): Boolean {
        // UPI ID validation rules
        val upiIdRegex = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")
        return upiId.contains("@") && !upiId.contains(" ") && upiIdRegex.matches(upiId)
    }

    private fun getInvalidUpiIdReasons(upiId: String): List<String> {
        val reasons = mutableListOf<String>()
        if (!upiId.contains("@")) {
            reasons.add("Must contain '@'") // Rule 2
        }
        if (upiId.contains(" ")) {
            reasons.add("Should not contain whitespace") // Rule 3
        }
        if (!Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$").matches(upiId)) {
            reasons.add("Invalid characters used") // Rule 1 (and implicitly covers Rule 4)
        }
        return reasons
    }
}