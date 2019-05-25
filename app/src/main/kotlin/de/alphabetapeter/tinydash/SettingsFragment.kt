package de.alphabetapeter.tinydash

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.PreferenceFragmentCompat
import de.alphabetapeter.tinydash.calendar.DeviceCalendarProvider
import de.alphabetapeter.tinydash.widget.TinyDashWidgetProvider
import de.alphabetapeter.tinydash.widget.WidgetPrefs


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val BUNDLE_WIDGET_ID = "WIDGET_ID"
    }

    private var appWidgetId: Int = 0

        override fun onCreatePreferences(savedInstanceState: Bundle?, p1: String?) {
        appWidgetId = arguments!!.getInt(BUNDLE_WIDGET_ID)
        addPreferencesFromResource(R.xml.preferences)
        bindClickListeners()
        reloadCalendarPermission()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            resources.getString(R.string.pref_key_display_first_big) -> reloadWidgetStyle()
        }
    }

    private fun sharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun bindClickListeners() {
        findPreference(getString(R.string.pref_key_enabled_calendars)).setOnPreferenceClickListener {
            pickEnabledCalendars()
            true
        }
    }

    private fun pickEnabledCalendars() {
        val calendar = DeviceCalendarProvider(context!!)
        val visibleCalendars = calendar.getVisibleCalendars()
        val selectedItems: MutableList<Int> = mutableListOf()
        val enabledCalendars = WidgetPrefs.getWidgetCalendarIds(context!!, appWidgetId)
        val checkedItems: BooleanArray = visibleCalendars
                .map { enabledCalendars!!.contains(it.id.toString()) }
                .toBooleanArray()
        showCalendarPickerDialog(checkedItems, selectedItems, visibleCalendars)
    }

    private fun showCalendarPickerDialog(
            checkedItems: BooleanArray,
            selectedItems: MutableList<Int>,
            visibleCalendars: List<DeviceCalendarProvider.Calendar>
    ) {
        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.title_enabled_calendars)
                .setMultiChoiceItems(visibleCalendars.map { it.name }.toTypedArray(), checkedItems
                ) { _, indexSelected, isChecked ->
                    if (isChecked) {
                        selectedItems.add(indexSelected)
                    } else if (selectedItems.contains(indexSelected)) {
                        selectedItems.remove(Integer.valueOf(indexSelected))
                    }
                }
                .setPositiveButton(getString(R.string.text_button_dialog_positive)
                ) { _, _ ->
                    val calendarIdList = selectedItems
                            .map { visibleCalendars[it].id.toString() }
                            .toHashSet()
                    WidgetPrefs.setWidgetCalendarIds(context!!, appWidgetId, calendarIdList)
                    TinyDashWidgetProvider.updateWidget(context!!)
                }
                .setNegativeButton(getString(R.string.text_button_dialog_negative), null)
                .create()
        dialog.show()
    }

    private fun reloadWidgetStyle() {
        WidgetPrefs.setWidgetFirstItemBig(
                context!!,
                appWidgetId,
                sharedPreferences()
                        .getBoolean(
                                resources.getString(R.string.pref_key_display_first_big),
                                true)
        )
    }

    private fun reloadCalendarPermission() {
        val calendar = DeviceCalendarProvider(context!!)
        if (calendar.hasPermission()) {
            TinyDashWidgetProvider.updateWidget(context!!)
        } else {
            calendar.requestPermission(activity!!, DeviceCalendarProvider.PERMISSIONS_REQUEST_READ_CALENDAR)
        }
    }

}
