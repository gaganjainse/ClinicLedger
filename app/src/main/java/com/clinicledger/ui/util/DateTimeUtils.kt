package com.clinicledger.ui.util

import java.util.*

/**
 * Utility for calendar calculations and start-of-period resolutions.
 */
object DateTimeUtils {

    /** Returns midnight of the current day */
    fun getStartOfDay(/** ref */ date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    /** Returns the first day of the current week */
    fun getStartOfWeek(/** ref */ date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = getStartOfDay(date)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.time
    }

    /** Returns the first day of the current month */
    fun getStartOfMonth(/** ref */ date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = getStartOfDay(date)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    /** Returns a date [days] into the past */
    fun getDaysAgo(/** count */ days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    /** Resolves localized day of week name */
    fun getLocalizedDayOfWeek(/** ref */ date: Date, /** state */ isHindi: Boolean): String {
        val cal = Calendar.getInstance()
        cal.time = date
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> if (isHindi) "रविवार" else "Sunday"
            Calendar.MONDAY -> if (isHindi) "सोमवार" else "Monday"
            Calendar.TUESDAY -> if (isHindi) "मंगलवार" else "Tuesday"
            Calendar.WEDNESDAY -> if (isHindi) "बुधवार" else "Wednesday"
            Calendar.THURSDAY -> if (isHindi) "गुरुवार" else "Thursday"
            Calendar.FRIDAY -> if (isHindi) "शुक्रवार" else "Friday"
            else -> if (isHindi) "शनिवार" else "Saturday"
        }
    }
}
