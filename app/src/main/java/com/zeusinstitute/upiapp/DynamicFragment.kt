package com.zeusinstitute.upiapp

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class DynamicFragment : Fragment() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var paymentIdTextView: TextView
    private lateinit var amountEditText: TextInputEditText
    private lateinit var submitButton: Button
    private lateinit var amountTextView: TextView

    private lateinit var paymentMethod: String
    private lateinit var currency: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dynamic, container, false)

        qrCodeImageView = view.findViewById(R.id.qrCodeImageView)
        paymentIdTextView = view.findViewById(R.id.upiIdTextView)
        amountEditText = view.findViewById(R.id.amountEditText)
        submitButton = view.findViewById(R.id.submitButton)
        amountTextView = view.findViewById(R.id.amountTextView)

        // Initially hide the submitButton
        submitButton.visibility = View.GONE

        // Add TextWatcher to amountEditText
        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                submitButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Get saved data from SharedPreferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedData = sharedPref.getString("saved_data", "") ?: ""
        paymentMethod = sharedPref.getString("payment_method", "") ?: ""
        currency = sharedPref.getString("currency", "") ?: ""

        paymentIdTextView.text = "$paymentMethod ID: $savedData"

        // Initially hide the amountTextView
        amountTextView.visibility = View.GONE

        // Initial QR code generation
        updateQRCode(savedData, "")

        // Set up the listener for the "Enter" key on the keyboard
        amountEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitButton.performClick()
                val imm = getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(amountEditText.windowToken, 0)
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            val amount = amountEditText.text.toString()

            // Show amount below savedData
            amountTextView.text = "Amount: ${getCurrencySymbol()}$amount"
            amountTextView.visibility = View.VISIBLE

            // Hide submitButton and clear amountEditText after QR generation
            submitButton.visibility = View.GONE
            amountEditText.text?.clear()

            // Update QR code
            updateQRCode(savedData, amount)
        }

        return view
    }

    private fun updateQRCode(savedData: String, amount: String) {
        val qrString = when (paymentMethod) {
            "SGQR" -> "sgqr://pay?merchantId=$savedData&$amount"
            "UPI" -> "upi://pay?pa=$savedData&tn=undefined&am=$amount"
            else -> ""
        }
        generateQRCode(qrString, qrCodeImageView)
    }

    private fun generateQRCode(text: String, imageView: ImageView) {
        val barcodeEncoder = BarcodeEncoder()
        try {
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
            imageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun getCurrencySymbol(): String {
        return when (currency) {
            "₹ (INR)" -> "₹"
            "S$ (SGD)" -> "S$"
            else -> ""
        }
    }
}