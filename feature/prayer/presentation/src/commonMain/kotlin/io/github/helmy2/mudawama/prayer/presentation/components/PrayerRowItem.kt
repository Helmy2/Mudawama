package io.github.helmy2.mudawama.prayer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus

@Composable
fun PrayerRowItem(
    prayer: PrayerWithStatus,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = prayer.name.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = prayer.timeString, style = MaterialTheme.typography.bodySmall)
        }
        
        IconButton(onClick = onToggle, modifier = Modifier.size(48.dp)) {
            val icon = when (prayer.status) {
                LogStatus.COMPLETED -> Icons.Filled.CheckCircle
                else -> Icons.Outlined.RadioButtonUnchecked
            }
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}
