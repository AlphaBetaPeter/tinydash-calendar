package de.alphabetapeter.tinydash

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.alphabetapeter.tinydash.calendar.DeviceCalendarProvider
import android.R.attr.fragment


class WidgetConfigurationActivity : AppCompatActivity() {

    private var appWidgetId = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar!!.setHomeAsUpIndicator(R.mipmap.ic_done_white_24dp)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (intent.extras != null) {
            appWidgetId = intent.extras!!.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }


        val fragment = SettingsFragment()
        val arguments = Bundle()
        arguments.putInt(SettingsFragment.BUNDLE_WIDGET_ID, appWidgetId)
        fragment.arguments = arguments

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            DeviceCalendarProvider.PERMISSIONS_REQUEST_READ_CALENDAR -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    val resultValue = Intent()
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_CANCELED, resultValue)
                    finish()
                }
            }
        }
    }
}