package de.alphabetapeter.tinydash.calendar

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import de.alphabetapeter.tinydash.widget.WidgetPrefs
import java.text.SimpleDateFormat
import java.util.*


class DeviceCalendarProvider(private val context: Context) {

	private val contentResolver : ContentResolver = context.contentResolver

	companion object {
		const val PERMISSIONS_REQUEST_READ_CALENDAR: Int = 682
	}

	data class Calendar(val id: Long, val name: String, val color: Int)
	data class Event(val id: Long, val title: String, val time: Long, val calendarId: Long)

	fun hasPermission() :Boolean {
		val permissionCheck = ContextCompat.checkSelfPermission(context,
				Manifest.permission.READ_CALENDAR)
		return permissionCheck == PackageManager.PERMISSION_GRANTED
	}

	fun requestPermission(activity: Activity, requestCode: Int) {
		ActivityCompat.requestPermissions(activity,
				arrayOf(Manifest.permission.READ_CALENDAR), requestCode)
	}

	fun getVisibleCalendars(): List<Calendar> {
		val calendarsMutable: MutableList<Calendar> = mutableListOf()
		val visibleCalendars = contentResolver.query(Calendars.CONTENT_URI,
				arrayOf(Calendars._ID,
						Calendars.NAME,
						Calendars.CALENDAR_COLOR,
						Calendars.ACCOUNT_NAME,
						Calendars.ACCOUNT_TYPE
				),
				Calendars.VISIBLE + " = 1",
				null,
				Calendars._ID + " ASC")
		while (visibleCalendars!!.moveToNext()) {
			val calendarId = visibleCalendars.getLong(0)
			val displayName = visibleCalendars.getString(1)
			val color = visibleCalendars.getInt(2)
			calendarsMutable.add(Calendar(calendarId, displayName, color))
		}
		visibleCalendars.close()

		return calendarsMutable
	}

	fun colorForCalendarId(id: Long): Int {
		return getVisibleCalendars().first { it.id == id }.color
	}

	fun getEventsForToday(widgetId: Int) : List<Event>{
		val events = mutableListOf<Event>()

		val enabledCalendarIds = WidgetPrefs.getWidgetCalendarIds(context, widgetId)!!
				.joinToString(", ")
		Log.d("enabledCalendarIds", enabledCalendarIds)
		val endTime = Long.MAX_VALUE
		val selectorProjection = arrayOf(
				EventsEntity._ID,
				EventsEntity.TITLE,
				EventsEntity.DTSTART,
				EventsEntity.CALENDAR_ID
		)
		val query = "${EventsEntity.CALENDAR_ID} IN ($enabledCalendarIds) " +
				"AND ${EventsEntity.DTSTART} > ${System.currentTimeMillis()} " +
				"AND ${EventsEntity.DTEND} < $endTime"
		val calendarEntries = contentResolver.query(
				EventsEntity.CONTENT_URI,
				selectorProjection,
				query,
				null,
				null
		)
		while(calendarEntries!!.moveToNext()){
			val eventId = calendarEntries.getLong(0)
			val eventTitle = calendarEntries.getString(1)
			val eventTime = calendarEntries.getLong(2)
			val calendarId = calendarEntries.getLong(3)
			events.add(Event(eventId, eventTitle, eventTime, calendarId))
		}
		calendarEntries.close()
		return events
	}

	fun getRecurringEvents(widgetId: Int): List<Event> {
		val startTime = System.currentTimeMillis()
		val endTime = Long.MAX_VALUE
		val events = mutableListOf<Event>()
		val selectorProjection = arrayOf(
				Instances.EVENT_ID,
				Instances.TITLE,
				Instances.BEGIN,
				Instances.CALENDAR_ID
		)
		val builder = Instances.CONTENT_URI.buildUpon()
		ContentUris.appendId(builder, startTime)
		ContentUris.appendId(builder, endTime)

		val enabledCalendarIds = WidgetPrefs.getWidgetCalendarIds(context, widgetId)!!.joinToString(", ")
		Log.d("enabledCalendarIds", enabledCalendarIds)
		val query = "${Instances.CALENDAR_ID} IN ($enabledCalendarIds) " +
				"AND ${Instances.BEGIN} > $startTime " +
				"AND ${Instances.END} < $endTime"

		val calendarEntries = contentResolver.query(
				builder.build(),
				selectorProjection,
				query, null, null
		)
		while(calendarEntries!!.moveToNext()){
			val eventId = calendarEntries.getLong(0)
			val eventTitle = calendarEntries.getString(1)
			val eventTime = calendarEntries.getLong(2)
			val calendarId = calendarEntries.getLong(3)
			events.add(Event(eventId, eventTitle, eventTime, calendarId))
		}
		calendarEntries.close()
		return events
	}

}