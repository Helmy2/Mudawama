package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}