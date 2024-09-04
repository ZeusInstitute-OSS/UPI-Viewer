package com.zeusinstitute.upiapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zeusinstitute.upiapp.databinding.FragmentFirstBinding
import kotlin.math.min

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
    private lateinit var loginButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var customAmountButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var splitBillButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var updateAppButton: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var checkWalletButton: com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        qrCodeImageView = binding.qrCodeImageView
        smsStatusTextView = binding.smsStatusTextView

        // Deal with Small Screens
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val aspectRatio = screenHeight.toFloat()/ screenWidth.toFloat()

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)

        Log.d("DisplayDetails", "screenWidth = ${screenWidth} and screenHeight=${screenHeight}")
        val heightPercent = if (aspectRatio >= 16f / 9f) 0.6f else 0.5f
        constraintSet.constrainPercentHeight(R.id.qrCodeImageView, heightPercent)
        constraintSet.applyTo(binding.root)

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

        loginButton = view.findViewById(R.id.loginButton)
        customAmountButton = view.findViewById(R.id.customAmountButton)
        splitBillButton = view.findViewById(R.id.splitBillButton)
        updateAppButton = view.findViewById(R.id.updateAppButton)
        checkWalletButton = view.findViewById(R.id.checkWalletButton)

        updateQRCode() // Generate QR code initially
        updateSmsStatus()

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_login)
        }
        customAmountButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_dynamicFragment)
        }
        splitBillButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_splitBillFragment)
        }
        checkWalletButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_billHistory)
        }
        updateAppButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_Update)
        }
        loginButton.setOnLongClickListener {
            Toast.makeText(context, "Set UPI Id", Toast.LENGTH_SHORT).show()
            true // Consume the long click
        }
        customAmountButton.setOnLongClickListener {
            Toast.makeText(context, "Custom Amount", Toast.LENGTH_SHORT).show()
            true // Consume the long click
        }

        splitBillButton.setOnLongClickListener {
            Toast.makeText(context, "Split the Bill", Toast.LENGTH_SHORT).show()
            true // Consume the long click
        }

        checkWalletButton.setOnLongClickListener {
            Toast.makeText(context, "Check Transaction History", Toast.LENGTH_SHORT).show()
            true // Consume the long click
        }

        updateAppButton.setOnLongClickListener {
            Toast.makeText(context, "Check for App Updates", Toast.LENGTH_SHORT).show()
            true // Consume the long click
        }
    }

    override fun onResume() {
        super.onResume()
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
        try {// Generate a high-resolution QR code (adjust size as needed)
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 800, 800)
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER // Let ImageView handle scaling
            imageView.setPadding(0, 0, 0, 0)
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
        val sharedPref = requireActivity().getSharedPreferences("com.zeusinstitute.upiapp.preferences", Context.MODE_PRIVATE)
        val smsEnabled = sharedPref.getBoolean("sms_enabled", false)
        val announceEnabled = sharedPref.getBoolean("announce_enabled", false)

        Log.d("FirstFragment", "SMS Enabled: $smsEnabled")
        Log.d("FirstFragment", "Announce Enabled: $announceEnabled")

        smsStatusTextView.text = if (smsEnabled && announceEnabled) "Speaker Mode Enabled" else ""
        smsStatusTextView.visibility = if (smsEnabled && announceEnabled) View.VISIBLE else View.GONE
    }
}