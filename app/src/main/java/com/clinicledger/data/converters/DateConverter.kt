package com.clinicledger.data.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room TypeConverter for Date serialization.
 */
class DateConverter {
    /** Converts Long to Date */
    @TypeConverter
    fun fromTimestamp(/** epoch */ value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /** Converts Date to Long */
    @TypeConverter
    fun dateToTimestamp(/** date */ date: Date?): Long? {
        return date?.time
    }
}
