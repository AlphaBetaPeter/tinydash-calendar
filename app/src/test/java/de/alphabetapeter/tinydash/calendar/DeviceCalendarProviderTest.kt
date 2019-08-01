package de.alphabetapeter.tinydash.calendar

import org.junit.Test
import java.text.SimpleDateFormat

class DeviceCalendarProviderTest {

    @Test
    fun testTimeRangeCurrentYear() {
        val cal = java.util.Calendar.getInstance();

        cal.set(java.util.Calendar.YEAR, cal.get(java.util.Calendar.YEAR) + 1)

        cal.time

        val sdf = SimpleDateFormat();

        println(sdf.format(cal.time))

    }
}