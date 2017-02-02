package de.alphabetapeter.tinydash

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import de.alphabetapeter.tinydash.calendar.DeviceCalendarProvider
import de.alphabetapeter.tinydash.widget.TinyDashWidgetProvider
import de.alphabetapeter.tinydash.widget.WidgetPrefs


class SettingsFragment(val appWidgetId: Int) : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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

	fun sharedPreferences(): SharedPreferences {
		return PreferenceManager.getDefaultSharedPreferences(context)
	}

	private fun bindClickListeners() {
		findPreference(getString(R.string.pref_key_enabled_calendars)).onPreferenceClickListener =
				Preference.OnPreferenceClickListener({
					pickEnabledCalendars()
					true
				})
		findPreference(getString(R.string.pref_key_time_span)).onPreferenceClickListener =
				Preference.OnPreferenceClickListener({
					showLookaheadTimePickerDialog()
					true
				})
	}

	private fun pickEnabledCalendars() {
		val calendar = DeviceCalendarProvider(context)
		val visibleCalendars = calendar.getVisibleCalendars()
		val selectedItems: MutableList<Int> = mutableListOf()
		val enabledCalendars = WidgetPrefs.getWidgetCalendarIds(context, appWidgetId)
		val checkedItems: BooleanArray = visibleCalendars
				.map { enabledCalendars.contains(it.id.toString()) }
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
				.setMultiChoiceItems(visibleCalendars.map { it.name }.toTypedArray(), checkedItems,
						{ dialog, indexSelected, isChecked ->
							if (isChecked) {
								selectedItems.add(indexSelected)
							} else if (selectedItems.contains(indexSelected)) {
								selectedItems.remove(Integer.valueOf(indexSelected))
							}
						})
				.setPositiveButton(getString(R.string.text_button_dialog_positive),
						{ dialog, id ->
							val calendarIdList = selectedItems
									.map { visibleCalendars[it].id.toString() }
									.toHashSet()
							WidgetPrefs.setWidgetCalendarIds(context, appWidgetId, calendarIdList)
							TinyDashWidgetProvider.updateWidget(context)
						})
				.setNegativeButton(getString(R.string.text_button_dialog_negative), null)
				.create()
		dialog.show()
	}

	private fun showLookaheadTimePickerDialog() {
		data class TimeSpan(val id: String, val time: Long, val stringResource: Int)

		val timeSpans = listOf(
				TimeSpan("24_HOURS", 1000 * 60 * 60 * 24, R.string.time_span_24_hours),
				TimeSpan("3_DAYS", 1000 * 60 * 60 * 24 * 3, R.string.time_span_3_days),
				TimeSpan("7_DAYS", 1000 * 60 * 60 * 24 * 7, R.string.time_span_7_days)
		)
		var selectedItem = 0
		val dialog = AlertDialog.Builder(context)
				.setTitle(R.string.title_time_span)
				.setSingleChoiceItems(timeSpans.map { getString(it.stringResource) }.toTypedArray(), selectedItem, {
					dialog, position ->
					selectedItem = position
				})
				.setPositiveButton(getString(R.string.text_button_dialog_positive),
						{ dialog, id ->
							WidgetPrefs.setWidgetTimeSpan(context, appWidgetId, timeSpans[selectedItem].time)
							TinyDashWidgetProvider.updateWidget(context)
						})
				.setNegativeButton(getString(R.string.text_button_dialog_negative), null)
				.create()
		dialog.show()
	}

	private fun reloadWidgetStyle() {
		WidgetPrefs.setWidgetFirstItemBig(
				context,
				appWidgetId,
				sharedPreferences()
						.getBoolean(
								resources.getString(R.string.pref_key_display_first_big),
								true)
		)
	}

	fun reloadCalendarPermission() {
		val calendar = DeviceCalendarProvider(context)
		if (calendar.hasPermission()) {
			TinyDashWidgetProvider.updateWidget(context)
		} else {
			calendar.requestPermission(activity, DeviceCalendarProvider.PERMISSIONS_REQUEST_READ_CALENDAR)
		}
	}

}
