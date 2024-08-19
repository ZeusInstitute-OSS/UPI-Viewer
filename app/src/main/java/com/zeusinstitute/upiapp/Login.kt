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
import android.widget.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Login : Fragment() {
    private lateinit var countrySpinner: Spinner
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var currencySpinner: Spinner
    private lateinit var apikeyLayout: TextInputLayout
    private lateinit var apikey: TextInputEditText
    private lateinit var rulesTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        apikeyLayout = view.findViewById(R.id.apikeyLayout)
        apikey = view.findViewById(R.id.apikey)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        rulesTextView = view.findViewById(R.id.rulesTextView)

        countrySpinner = view.findViewById(R.id.countrySpinner)
        paymentMethodSpinner = view.findViewById(R.id.paymentMethodSpinner)
        currencySpinner = view.findViewById(R.id.currencySpinner)

        setupSpinners()

        // Show UPI rules initially
        updateUIForPaymentMethod("UPI")
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

        // Set up the listener for the "Enter" key on the keyboard
        apikey.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitButton.performClick()

                // Request focus on the apikey field and show the keyboard
                apikey.requestFocus()

                // Hide Keyboard
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(apikey.windowToken, 0)
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            handleSubmit()
        }

        return view
    }


    private fun setupSpinners() {
        // Set up country spinner
        val countries = resources.getStringArray(R.array.countries)
        val countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countries)
        countrySpinner.adapter = countryAdapter

        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCountry = countries[position]
                updatePaymentMethodSpinner(selectedCountry)
                updateCurrencySpinner(selectedCountry)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set up payment method spinner listener
        paymentMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPaymentMethod = parent?.getItemAtPosition(position).toString()
                updateUIForPaymentMethod(selectedPaymentMethod)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Initial setup for payment method and currency
        updatePaymentMethodSpinner(countries[0])
        updateCurrencySpinner(countries[0])
    }


    private fun updateUIForPaymentMethod(paymentMethod: String) {
        if (paymentMethod == "UPI") {
            rulesTextView.visibility = View.VISIBLE
            //apikey.hint = "Enter UPI ID"
        } else {
            rulesTextView.visibility = View.GONE
           //apikey.hint = "Enter Payment ID"
        }
    }

    private fun updatePaymentMethodSpinner(country: String) {
        val paymentMethodsResId = when (country) {
            "India" -> R.array.payment_methods_india
           // "Singapore" -> R.array.payment_methods_singapore
            else -> return
        }
        val paymentMethods = resources.getStringArray(paymentMethodsResId)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, paymentMethods)
        paymentMethodSpinner.adapter = adapter
    }

    private fun updateCurrencySpinner(country: String) {
        val currenciesResId = when (country) {
            "India" -> R.array.currencies_india
          //  "Singapore" -> R.array.currencies_singapore
            else -> return
        }
        val currencies = resources.getStringArray(currenciesResId)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currencySpinner.adapter = adapter
    }

    private fun handleSubmit() {
        val data = apikey.text.toString()
        val country = countrySpinner.selectedItem.toString()
        val paymentMethod = paymentMethodSpinner.selectedItem.toString()
        val currency = currencySpinner.selectedItem.toString()

        if (paymentMethod == "UPI") {
            val invalidReasons = getInvalidUpiIdReasons(data)
            if (invalidReasons.isNotEmpty()) {
                apikeyLayout.error = "Invalid UPI ID:\n${invalidReasons.joinToString("\n")}"
                return
            }
        }

        // Save data to Shared Preferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("saved_data", data)
            putString("country", country)
            putString("payment_method", paymentMethod)
            putString("currency", currency)
            apply()
        }

        // Navigate back to FirstFragment
        findNavController().navigate(R.id.action_login_to_firstFragment)
    }

    private fun getInvalidUpiIdReasons(upiId: String): List<String> {
        val reasons = mutableListOf<String>()
        if (!upiId.contains("@")) {
            reasons.add("Must contain '@'")
        }
        if (upiId.contains(" ")) {
            reasons.add("Should not contain whitespace")
        }
        if (!Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$").matches(upiId)) {
            reasons.add("Invalid characters used")
        }
        return reasons
    }
}