package com.example.upiapp

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class UpdateFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}