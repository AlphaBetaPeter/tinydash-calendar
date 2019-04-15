package de.alphabetapeter.tinydash.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TinyDashMonthRemoteService : RemoteViewsService() {

	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
		return CalenderMonthListViewFactory(this.applicationContext, intent)
	}

}