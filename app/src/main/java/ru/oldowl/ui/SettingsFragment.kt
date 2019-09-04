package ru.oldowl.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.view.View
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.BuildConfig
import ru.oldowl.R
import ru.oldowl.core.extension.browse
import ru.oldowl.core.extension.findPreference
import ru.oldowl.repository.SyncManager

class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {

    private val syncManager: SyncManager by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.nav_settings)

        val versionName = findPreference(R.string.key_version_name)
        versionName.summary = BuildConfig.VERSION_NAME

        val sendFeedback = findPreference(R.string.key_send_feedback)
        sendFeedback.setOnPreferenceClickListener {
            context?.browse(BuildConfig.SEND_FEEDBACK_URL)
            true
        }

        val autoUpdate = findPreference(R.string.key_auto_update)
        autoUpdate.setOnPreferenceChangeListener { _, _ ->
            syncManager.scheduleUpdate()
            true
        }

        val autoUpdatePeriod = findPreference(R.string.key_auto_update_period)
        autoUpdatePeriod.setOnPreferenceChangeListener { _, _ ->
            syncManager.scheduleUpdate()
            true
        }
    }
}