package de.alphabetapeter.tinydash.widget

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import de.alphabetapeter.tinydash.R

class WidgetPrefs {

	companion object {
		val DEBUG_TAG: String = WidgetPrefs::class.java.simpleName

		fun getWidgetCalendarIds(context: Context, widgetId: Int): MutableSet<String>? {
			return sharedPreferences(context)
					.getStringSet(
							keyEnabledCalendars(context, widgetId),
							setOf()
					)
		}

		fun setWidgetCalendarIds(context: Context, widgetId: Int, calendarIdList: Set<String>) {
			sharedPreferences(context)
					.edit()
					.putStringSet(
							keyEnabledCalendars(context, widgetId),
							calendarIdList
					)
					.apply()
		}

		fun getWidgetFirstItemBig(context: Context, widgetId: Int): Boolean {
			return sharedPreferences(context)
					.getBoolean(
							keyFirstItemBig(context, widgetId),
							true
					)
		}

		fun setWidgetFirstItemBig(context: Context, widgetId: Int, isBig: Boolean) {
			sharedPreferences(context)
					.edit()
					.putBoolean(
							keyFirstItemBig(context, widgetId),
							isBig
					)
					.apply()
		}

		private fun keyEnabledCalendars(context: Context, widgetId: Int): String {
			return prefKeyWithWidgetId(context.getString(R.string.pref_key_enabled_calendars), widgetId)
		}

		private fun keyFirstItemBig(context: Context, widgetId: Int): String {
			return prefKeyWithWidgetId(context.getString(R.string.pref_key_display_first_big), widgetId)
		}

		private fun prefKeyWithWidgetId(key: String, widgetId: Int): String {
			return "${key}_widget_$widgetId"
		}

		private fun sharedPreferences(context: Context): SharedPreferences {
			return PreferenceManager.getDefaultSharedPreferences(context)
		}
	}
}