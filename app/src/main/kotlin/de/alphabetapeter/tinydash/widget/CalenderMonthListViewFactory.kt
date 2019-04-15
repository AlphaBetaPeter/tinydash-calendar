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

class CalenderMonthListViewFactory(private val context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var appWidgetId: Int = 0
    private val calendar = DeviceCalendarProvider(context)

    private var calendarEntries: List<ListEntry>? = null

    companion object {
        private const val TIME_FORMAT_DAY_TIME_24H = "EEEE d MMM HH:mm"
        private const val TIME_FORMAT_DAY_OF_MONTH_SHORT = "EEE d."
        private const val TIME_FORMAT_MONTH = "MMMM"
    }

    init {
        if (intent.extras != null) {
            appWidgetId = intent.extras!!.getInt(
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
        return 3
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
        return if (showFirstItemBig()) {
            when (position) {
                0 -> buildBigRowView(entry.event!!)
                else -> renderEntryOrMonth(entry)
            }
        } else {
            renderEntryOrMonth(entry)
        }
    }

    private fun renderEntryOrMonth(entry: ListEntry) =
            if (entry.event !== null) buildRowView(entry.event) else buildMonthRowView(entry.title)


    private fun showFirstItemBig(): Boolean {
        return WidgetPrefs.getWidgetFirstItemBig(context, appWidgetId)
    }

    private fun buildBigRowView(entry: DeviceCalendarProvider.Event): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.calendar_row_next)
        row.setTextViewText(R.id.calendar_row_time, formatEventDayAndTime(entry))
        row.setTextViewText(R.id.calendar_row_text, entry.title)
        return row
    }

    private fun buildRowView(entry: DeviceCalendarProvider.Event): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.calendar_row_single)
        row.setTextViewText(R.id.calendar_row_text, "${formatEventDayAndTimeShort(entry)} ${entry.title} ")
        return row
    }

    private fun buildMonthRowView(month: String): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.calendar_row_month)
        row.setTextViewText(R.id.calendar_row_month_text, month)
        return row
    }

    private fun formatEventDayAndTime(entry: DeviceCalendarProvider.Event): String {
        val dateFormat = SimpleDateFormat(TIME_FORMAT_DAY_TIME_24H, Locale.UK)
        return dateFormat.format(entry.time)
    }

    private fun formatEventDayAndTimeShort(entry: DeviceCalendarProvider.Event): String {
        val dateFormat = SimpleDateFormat(TIME_FORMAT_DAY_OF_MONTH_SHORT, Locale.UK)
        return dateFormat.format(entry.time)
    }

    private fun formatEventMonth(entry: DeviceCalendarProvider.Event): String {
        val dateFormat = SimpleDateFormat(TIME_FORMAT_MONTH, Locale.UK)
        return dateFormat.format(entry.time)
    }


    private fun loadCalendarEntries() {
        if (!calendar.hasPermission()) {
            Toast.makeText(
                    context,
                    context.getString(R.string.calendar_permission_not_granted),
                    Toast.LENGTH_SHORT
            ).show()
        }
        val rawEvents = calendar.getEventsForToday(appWidgetId)
                .plus(calendar.getRecurringEvents(appWidgetId))
                .sortedWith(compareBy { it.time })
                .distinctBy { it.id }


        val groupedEvents = groupByMonth(rawEvents)

        calendarEntries = groupedEvents.minus(groupedEvents[0])

        Log.d(CalenderMonthListViewFactory::class.java.simpleName, "grouped $groupedEvents")


    }

    data class ListEntry(val event: DeviceCalendarProvider.Event?, val title: String)

    private fun groupByMonth(rawEvents: List<DeviceCalendarProvider.Event>): List<ListEntry> {

        val groupedByMonth: Map<String, List<DeviceCalendarProvider.Event>> = rawEvents.groupBy {
            formatEventMonth(it)
        }

        return groupedByMonth.keys.map { month ->
            val monthEvents = groupedByMonth.getValue(month)

            val elements = monthEvents.map { e ->
                ListEntry(e, "")
            }

            listOf(
                    ListEntry(null, month)
            ).plus(elements)
        }.flatten()
    }

}