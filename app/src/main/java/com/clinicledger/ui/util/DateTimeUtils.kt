package com.clinicledger.ui.util

import java.util.*

object DateTimeUtils {

    fun getStartOfDay(date: Date = Date()): Date {
        val cal = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }

    fun getStartOfWeek(date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysSinceMonday = ((dayOfWeek - Calendar.MONDAY) + 7) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        return getStartOfDay(cal.time)
    }

    fun getStartOfMonth(date: Date = Date()): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return getStartOfDay(cal.time)
    }

    fun getDaysAgo(days: Int): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    fun getLocalizedDayOfWeek(date: Date, isHindi: Boolean): String {
        val cal = Calendar.getInstance()
        cal.time = date
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> if (isHindi) "रविवार" else "Sunday"
            Calendar.MONDAY -> if (isHindi) "सोमवार" else "Monday"
            Calendar.TUESDAY -> if (isHindi) "मंगलवार" else "Tuesday"
            Calendar.WEDNESDAY -> if (isHindi) "बुधवार" else "Wednesday"
            Calendar.THURSDAY -> if (isHindi) "गुरुवार" else "Thursday"
            Calendar.FRIDAY -> if (isHindi) "शुक्रवार" else "Friday"
            Calendar.SATURDAY -> if (isHindi) "शनिवार" else "Saturday"
            else -> ""
        }
    }
}
