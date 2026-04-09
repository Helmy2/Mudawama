package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * 7-day horizontal date strip centred on today (matches daily_prayer_tracker.png).
 *
 * Each pill shows:
 *   - Day abbreviation (SAT, SUN …) in labelSmall, letter-spaced
 *   - Day number in bodyLarge bold
 * Active day: deep-teal fill + white text + small white dot below.
 * Inactive day: surfaceVariant fill + onSurfaceVariant text.
 */
@Composable
fun PrayerDateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(dates) { date ->
            DateChip(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { onDateSelected(date) },
            )
        }
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val dayAbbrev = when (date.dayOfWeek) {
        DayOfWeek.MONDAY    -> "MON"
        DayOfWeek.TUESDAY   -> "TUE"
        DayOfWeek.WEDNESDAY -> "WED"
        DayOfWeek.THURSDAY  -> "THU"
        DayOfWeek.FRIDAY    -> "FRI"
        DayOfWeek.SATURDAY  -> "SAT"
        DayOfWeek.SUNDAY    -> "SUN"
    }

    val bgColor   = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = dayAbbrev,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = TextUnit(0.5f, TextUnitType.Sp),
                fontWeight = FontWeight.Medium,
            ),
            color = textColor,
        )
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = textColor,
        )
        // Today indicator dot
        if (isToday) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}
