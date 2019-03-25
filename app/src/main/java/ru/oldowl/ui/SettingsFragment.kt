package ru.oldowl.ui

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import ru.oldowl.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.nav_settings)
    }
}