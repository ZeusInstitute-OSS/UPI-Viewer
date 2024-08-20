package com.zeusinstitute.upiapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zeusinstitute.upiapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */


class FirstFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var qrCodeImageView: ImageView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var paymentMethod: String
    private lateinit var smsStatusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        qrCodeImageView = binding.qrCodeImageView
        smsStatusTextView = binding.smsStatusTextView

        // Make status bar transparent (conditionally for Lollipop and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = requireActivity().window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        sharedPref.registerOnSharedPreferenceChangeListener(this) // Register listener

        updateQRCode() // Generate QR code initially
        updateSmsStatus()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "saved_data") {
            updateQRCode() // Regenerate QR code when "saved_data" changes
        }
        if (key == "sms_enabled") {
            updateSmsStatus() // Check if SMS reading is enabled
        }
    }

    private fun updateQRCode() {
        val savedData = sharedPref.getString("saved_data", null)
        paymentMethod = sharedPref.getString("payment_method", "") ?: ""
        val upiString = when (paymentMethod) {
            //"SGQR" -> "sgqr://pay?merchantId=$savedData"
            "UPI" -> "upi://pay?pa=$savedData&tn=undefined"
            else -> ""
        }
        if (savedData != null) {
            binding.textView2.text = savedData
            generateQRCode(upiString, qrCodeImageView)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPref.unregisterOnSharedPreferenceChangeListener(this) // Unregister listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(requireActivity().window.decorView)
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            val window = requireActivity().window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT // or any other color you want
        }
        _binding = null
    }

    private fun updateSmsStatus() {
        val smsEnabled = sharedPref.getBoolean("sms_enabled", false)
        smsStatusTextView.text = if (smsEnabled) "Speaker Mode Enabled" else ""
        smsStatusTextView.visibility = if (smsEnabled) View.VISIBLE else View.GONE
    }
}