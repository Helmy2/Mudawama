package io.github.helmy2.mudawama.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.helmy2.mudawama.designsystem.MudawamaTheme

/**
 * A simple rectangular skeleton placeholder used during loading states.
 * Uses [MaterialTheme.shapes.medium] for corners (via the Modifier.clip applied at call site).
 */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            color = MudawamaTheme.colors.onSurface.copy(alpha = 0.08f),
        )
    )
}
