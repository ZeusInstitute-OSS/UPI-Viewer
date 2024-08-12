package com.zeusinstitute.upiapp

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class DynamicFragment : Fragment() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var upiIdTextView: TextView
    private lateinit var amountEditText: TextInputEditText
    private lateinit var submitButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dynamic, container, false)

        qrCodeImageView = view.findViewById(R.id.qrCodeImageView)
        upiIdTextView = view.findViewById(R.id.upiIdTextView)
        amountEditText = view.findViewById(R.id.amountEditText)
        submitButton = view.findViewById(R.id.submitButton)

        // Get saved UPI ID from SharedPreferences
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedData = sharedPref.getString("saved_data", "") ?: ""
        upiIdTextView.text = savedData

        // Initial QR code generation
        updateQRCode(savedData, "")

        submitButton.setOnClickListener {
            val amount = amountEditText.text.toString()
            updateQRCode(savedData, amount)
        }

        return view
    }

    private fun updateQRCode(savedData: String, amount: String) {
        val upiString = "upi://pay?pa=$savedData&tn=undefined&am=$amount"
        generateQRCode(upiString, qrCodeImageView)
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
}