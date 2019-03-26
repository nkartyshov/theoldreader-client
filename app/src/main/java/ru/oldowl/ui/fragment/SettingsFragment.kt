package ru.oldowl.ui.fragment

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import ru.oldowl.BuildConfig
import ru.oldowl.Jobs
import ru.oldowl.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.nav_settings)

        val versionNameKey = getString(R.string.key_version_name)
        val versionPreference = findPreference(versionNameKey)
        versionPreference.summary = BuildConfig.VERSION_NAME

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            when(key) {
                // FIXME Open github issue
                getString(R.string.key_send_feedback) -> null
                getString(R.string.key_auto_update) -> Jobs.scheduleUpdate(context!!)
            }
        }
    }
}