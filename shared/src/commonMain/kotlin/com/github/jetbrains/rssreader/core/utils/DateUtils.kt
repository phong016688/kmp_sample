package com.github.jetbrains.rssreader.core.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

object DateUtils {
    private val dateFormat = LocalDateTime.Format {
        hour()
        char('-')
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        year()
    }

    fun formatDate(date: Long): String {
        return dateFormat.format(
            Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    fun parseDate(dateString: String): LocalDateTime {
        return dateFormat.parse(dateString)
    }
}