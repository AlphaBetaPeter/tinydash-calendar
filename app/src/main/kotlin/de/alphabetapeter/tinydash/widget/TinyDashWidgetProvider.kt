package de.alphabetapeter.tinydash.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import de.alphabetapeter.tinydash.R

abstract class TinyDashWidgetProvider : AppWidgetProvider() {

	val DEBUG_TAG: String = TinyDashWidgetProvider::class.java.simpleName

	companion object {
		val ACTION_APPWIDGET_UPDATE = "de.alphabetapeter.tinydash.action.APPWIDGET_UPDATE"
		fun updateWidget(context: Context) {
			val widgetManager = AppWidgetManager.getInstance(context)
			val widgetComponent = ComponentName(context, TinyDashWidgetProvider::class.java)
			val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)
			val update = Intent()
			update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
			update.action = ACTION_APPWIDGET_UPDATE
			context.sendBroadcast(update)
		}
	}

	override fun onReceive(context: Context, intent: Intent) {
		Log.d(DEBUG_TAG, "onReceive ${intent.action}")
		super.onReceive(context, intent)
		val appWidgetManager = AppWidgetManager.getInstance(context)
		val thisAppWidget = ComponentName(context.packageName, TinyDashWidgetProvider::class.java.name)
		val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
		onUpdate(context, appWidgetManager, appWidgetIds)
	}

	override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
		Log.d(DEBUG_TAG, "onAppWidgetOptionsChanged")
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
		onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
	}

	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		Log.d(DEBUG_TAG, "onDeleted")
		super.onDeleted(context, appWidgetIds)
	}

	override fun onEnabled(context: Context) {
		Log.d(DEBUG_TAG, "onEnabled")
		super.onEnabled(context)
	}

	override fun onDisabled(context: Context) {
		Log.d(DEBUG_TAG, "onDisabled")
		super.onDisabled(context)
	}

	override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
		Log.d(DEBUG_TAG, "onRestored")
		super.onRestored(context, oldWidgetIds, newWidgetIds)
	}

	override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
		super.onUpdate(context, appWidgetManager, appWidgetIds)
		for (widgetId in appWidgetIds) {
			Log.d(DEBUG_TAG, "onUpdate widgetId: $widgetId")

			val svcIntent = Intent(context, TinyDashRemoteService::class.java)

			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
			svcIntent.data = Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME))

			val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
			remoteViews.setRemoteAdapter(R.id.calendarEventList, svcIntent)

			appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.calendarEventList)
			appWidgetManager.updateAppWidget(widgetId, remoteViews)
		}
	}
}
