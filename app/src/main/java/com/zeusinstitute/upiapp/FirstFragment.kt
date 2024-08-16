package com.zeusinstitute.upiapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        qrCodeImageView = binding.qrCodeImageView // Initialize qrCodeImageView

        // Make status bar transparent
        val window = requireActivity().window
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        sharedPref.registerOnSharedPreferenceChangeListener(this) // Register listener

        updateQRCode() // Generate QR code initially
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "saved_data") {
            updateQRCode() // Regenerate QR code when "saved_data" changes
        }
    }

    private fun updateQRCode() {
        val savedData = sharedPref.getString("saved_data", null)
        val upiString = "upi://pay?pa=$savedData&tn=undefined&am=undefined"

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
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(requireActivity().window.decorView)
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        val window = requireActivity().window
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT // or any other color you want
        _binding = null
    }
}