package de.alphabetapeter.tinydash.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TinyDashRemoteService : RemoteViewsService() {

	override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
		return CalenderListViewFactory(this.applicationContext, intent)
	}

}