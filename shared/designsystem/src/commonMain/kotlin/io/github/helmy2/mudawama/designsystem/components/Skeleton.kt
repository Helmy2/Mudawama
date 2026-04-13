package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        )
    )
}