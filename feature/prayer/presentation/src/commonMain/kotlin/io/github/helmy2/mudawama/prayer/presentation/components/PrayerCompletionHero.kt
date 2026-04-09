package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus

@Composable
fun PrayerCompletionHero(
    prayers: List<PrayerWithStatus>,
    modifier: Modifier = Modifier
) {
    val completedCount = prayers.count { it.status == LogStatus.COMPLETED }
    val progress = if (prayers.isNotEmpty()) completedCount.toFloat() / prayers.size else 0f

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(64.dp))
            Text(text = "$completedCount/${prayers.size}")
        }
        Spacer(Modifier.width(16.dp))
        Text(text = "Prayers Completed Today", style = MaterialTheme.typography.titleMedium)
    }
}
