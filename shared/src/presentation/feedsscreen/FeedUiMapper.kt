package com.jjcr11.rss.presentation.feedsscreen

import com.jjcr11.rss.domain.model.Feed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

val FeedDateFormat: DateTimeFormat<LocalDate> = LocalDate.Format {
    monthName(names = MonthNames.ENGLISH_ABBREVIATED)
    char(value = ' ')
    day(padding = Padding.NONE)
    chars(value = ", ")
    year()
}

fun Feed.toUiModel(sourceName: String = ""): FeedUiModel = FeedUiModel(
    id = this.id.toString(),
    sourceName = sourceName,
    title = this.title,
    date = this.date.toDisplayDate(),
    url = this.url,
    content = this.content,
)

fun Instant.toDisplayDate(
    timeZone: TimeZone = TimeZone.UTC,
): String {
    val localDate = toLocalDateTime(timeZone = timeZone).date
    return FeedDateFormat.format(value = localDate)
}
