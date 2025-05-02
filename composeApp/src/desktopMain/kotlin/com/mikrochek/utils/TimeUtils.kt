package com.mikrochek.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.let

object TimeUtils {
    private val IST_ZONE_ID: ZoneId = ZoneId.of("Asia/Kolkata")
    private val DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val DEFAULT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val DEFAULT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
    
    /**
     * Get current timestamp in IST
     * @return current timestamp in milliseconds in IST timezone
     */
    fun getCurrentISTTimestamp(): Long {
        return ZonedDateTime.now(IST_ZONE_ID).toInstant().toEpochMilli()
    }
    
    /**
     * Convert timestamp to IST LocalDateTime
     * @param timestamp timestamp in milliseconds
     * @return LocalDateTime in IST timezone
     */
    fun timestampToISTDateTime(timestamp: Long): LocalDateTime {
        return Instant.ofEpochMilli(timestamp)
            .atZone(IST_ZONE_ID)
            .toLocalDateTime()
    }
    
    /**
     * Format timestamp to date string
     * @param timestamp timestamp in milliseconds
     * @param pattern optional date pattern (default: "dd MMM yyyy")
     * @return formatted date string
     */
    fun formatDate(timestamp: Long, pattern: String? = null): String {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) } ?: DEFAULT_DATE_FORMAT
        return timestampToISTDateTime(timestamp).format(formatter)
    }
    
    /**
     * Format timestamp to time string
     * @param timestamp timestamp in milliseconds
     * @param pattern optional time pattern (default: "HH:mm:ss")
     * @return formatted time string
     */
    fun formatTime(timestamp: Long, pattern: String? = null): String {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) } ?: DEFAULT_TIME_FORMAT
        return timestampToISTDateTime(timestamp).format(formatter)
    }
    
    /**
     * Format timestamp to datetime string
     * @param timestamp timestamp in milliseconds
     * @param pattern optional datetime pattern (default: "dd MMM yyyy HH:mm:ss")
     * @return formatted datetime string
     */
    fun formatDateTime(timestamp: Long, pattern: String? = null): String {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) } ?: DEFAULT_DATETIME_FORMAT
        return timestampToISTDateTime(timestamp).format(formatter)
    }
    
    /**
     * Check if two timestamps are on the same day
     * @param timestamp1 first timestamp in milliseconds
     * @param timestamp2 second timestamp in milliseconds
     * @return true if both timestamps are on the same day
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val date1 = timestampToISTDateTime(timestamp1).toLocalDate()
        val date2 = timestampToISTDateTime(timestamp2).toLocalDate()
        return date1 == date2
    }
    
    /**
     * Get start of day timestamp
     * @param timestamp timestamp in milliseconds
     * @return timestamp at start of the day (00:00:00)
     */
    fun getStartOfDay(timestamp: Long): Long {
        return timestampToISTDateTime(timestamp)
            .toLocalDate()
            .atStartOfDay(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Get end of day timestamp
     * @param timestamp timestamp in milliseconds
     * @return timestamp at end of the day (23:59:59.999)
     */
    fun getEndOfDay(timestamp: Long): Long {
        return timestampToISTDateTime(timestamp)
            .toLocalDate()
            .atTime(LocalTime.MAX)
            .atZone(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Add duration to timestamp
     * @param timestamp base timestamp in milliseconds
     * @param amount amount to add
     * @param unit ChronoUnit for the amount
     * @return new timestamp after adding duration
     */
    fun addDuration(timestamp: Long, amount: Long, unit: ChronoUnit): Long {
        return timestampToISTDateTime(timestamp)
            .plus(amount, unit)
            .atZone(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Get duration between two timestamps
     * @param start start timestamp in milliseconds
     * @param end end timestamp in milliseconds
     * @param unit ChronoUnit for the result
     * @return duration between timestamps in specified unit
     */
    fun getDuration(start: Long, end: Long, unit: ChronoUnit): Long {
        val startDateTime = timestampToISTDateTime(start)
        val endDateTime = timestampToISTDateTime(end)
        return unit.between(startDateTime, endDateTime)
    }
    
    /**
     * Parse date string to timestamp
     * @param dateStr date string
     * @param pattern date pattern
     * @return timestamp in milliseconds
     */
    fun parseDate(dateStr: String, pattern: String): Long {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDate.parse(dateStr, formatter)
            .atStartOfDay(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Parse datetime string to timestamp
     * @param dateTimeStr datetime string
     * @param pattern datetime pattern
     * @return timestamp in milliseconds
     */
    fun parseDateTime(dateTimeStr: String, pattern: String): Long {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(dateTimeStr, formatter)
            .atZone(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Get first day of month timestamp
     * @param timestamp timestamp in milliseconds
     * @return timestamp of first day of the month
     */
    fun getFirstDayOfMonth(timestamp: Long): Long {
        return timestampToISTDateTime(timestamp)
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
    
    /**
     * Get last day of month timestamp
     * @param timestamp timestamp in milliseconds
     * @return timestamp of last day of the month at 23:59:59.999
     */
    fun getLastDayOfMonth(timestamp: Long): Long {
        return timestampToISTDateTime(timestamp)
            .withDayOfMonth(timestampToISTDateTime(timestamp).toLocalDate().lengthOfMonth())
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(999999999)
            .atZone(IST_ZONE_ID)
            .toInstant()
            .toEpochMilli()
    }
} 