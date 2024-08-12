package com.zeusinstitute.upiapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.zeusinstitute.upiapp.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide system bars for fullscreen
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(requireActivity().window.decorView)
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())


        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedData = sharedPref.getString("saved_data", null)


        if (savedData != null) {
            binding.textView2.text = savedData
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(requireActivity().window.decorView)
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        _binding = null
    }
}