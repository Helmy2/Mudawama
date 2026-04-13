package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import io.github.helmy2.mudawama.designsystem.borderRadius
import io.github.helmy2.mudawama.designsystem.elevation

@Composable
fun MudawamaSurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MaterialTheme.borderRadius.extraLarge),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MaterialTheme.elevation.low,
            shadowElevation = MaterialTheme.elevation.subtle,
            onClick = onClick,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MaterialTheme.elevation.low,
            shadowElevation = MaterialTheme.elevation.subtle,
        ) {
            content()
        }
    }
}
