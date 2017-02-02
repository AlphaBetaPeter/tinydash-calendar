package de.alphabetapeter.tinydash.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import de.alphabetapeter.tinydash.R
import de.alphabetapeter.tinydash.calendar.DeviceCalendarProvider
import java.text.SimpleDateFormat
import java.util.*
import kotlin.comparisons.compareBy

class CalenderListViewFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

	private var appWidgetId: Int = 0
	private val calendar = DeviceCalendarProvider(context)
	private val TIME_FORMAT_DAY_TIME_24H = "EEEE d MMM HH:mm"
	private val TIME_FORMAT_DAY_TIME_12H = "EEEE d MMM hh:mm"
	private val TIME_FORMAT_TIME_24H = "HH:mm"
	private var calendarEntries: List<DeviceCalendarProvider.Event>? = null

	init {
		if (intent.extras != null) {
			appWidgetId = intent.extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID
			)
		}
	}

	override fun onCreate() {
		loadCalendarEntries()
	}

	override fun onDestroy() {
	}

	override fun getCount(): Int {
		return calendarEntries!!.size
	}


	override fun getLoadingView(): RemoteViews? {
		return null
	}

	override fun getViewTypeCount(): Int {
		return 2
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun hasStableIds(): Boolean {
		return true
	}

	override fun onDataSetChanged() {
		loadCalendarEntries()
	}

	override fun getViewAt(position: Int): RemoteViews? {
		val entries = calendarEntries!!.toTypedArray()
		val entry = entries[position]
		if(showFirstItemBig()){
			when (position) {
				0 -> return buildTodayRowView(entry)
				else -> return buildRegularRowView(entry)
			}
		} else {
			when (position) {
				0 -> return buildTodayRowViewRegularSize(entry)
				else -> return buildRegularRowView(entry)
			}
		}
	}

	fun showFirstItemBig() : Boolean {
		return WidgetPrefs.getWidgetFirstItemBig(context, appWidgetId)
	}

	private fun buildTodayRowView(entry: DeviceCalendarProvider.Event): RemoteViews {
		val row = RemoteViews(context.packageName, R.layout.calendar_row_next)
		row.setTextViewText(R.id.calendar_row_time, formatEventDayAndTime(entry))
		row.setTextViewText(R.id.calendar_row_text, entry.title)
		return row
	}

	private fun buildRegularRowView(entry: DeviceCalendarProvider.Event): RemoteViews {
		val row = RemoteViews(context.packageName, R.layout.calendar_row)
		row.setTextViewText(R.id.calendar_row_time, formatEventTime(entry))
		row.setTextViewText(R.id.calendar_row_text, entry.title)
		return row
	}

	private fun buildTodayRowViewRegularSize(entry: DeviceCalendarProvider.Event): RemoteViews {
		val row = RemoteViews(context.packageName, R.layout.calendar_row)
		row.setTextViewText(R.id.calendar_row_time, formatEventDayAndTime(entry))
		row.setTextViewText(R.id.calendar_row_text, entry.title)
		return row
	}

	private fun formatEventDayAndTime(entry: DeviceCalendarProvider.Event): String? {
		val dateFormat = SimpleDateFormat(TIME_FORMAT_DAY_TIME_24H, Locale.UK)
		return dateFormat.format(entry.time)
	}

	private fun formatEventTime(entry: DeviceCalendarProvider.Event): String? {
		val dateFormat = SimpleDateFormat(TIME_FORMAT_TIME_24H, Locale.UK)
		return dateFormat.format(entry.time)
	}

	fun loadCalendarEntries() {
		if(!calendar.hasPermission()){
			Toast.makeText(
					context,
					context.getString(R.string.calendar_permission_not_granted),
					Toast.LENGTH_SHORT
			).show()
		}
		calendarEntries = calendar.getEventsForToday(appWidgetId)
				.plus(calendar.getRecurringEvents(appWidgetId))
				.sortedWith(compareBy({ it.time }))
				.distinctBy { it.id }
		Log.d("asda", "events")
		calendarEntries!!.forEach { Log.d("asda", it.toString()) }
	}

}