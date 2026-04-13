package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun MudawamaDateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        DayOfWeek.MONDAY    -> "M"
        DayOfWeek.TUESDAY   -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY  -> "T"
        DayOfWeek.FRIDAY    -> "F"
        DayOfWeek.SATURDAY  -> "S"
        DayOfWeek.SUNDAY    -> "S"
    }

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = dayAbbrev,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = textColor.copy(alpha = 0.7f),
        )
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = textColor,
        )
        if (isToday) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    ),
            )
        }
    }
}