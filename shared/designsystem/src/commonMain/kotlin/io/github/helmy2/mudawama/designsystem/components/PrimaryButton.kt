package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.helmy2.mudawama.designsystem.spacing
import io.github.helmy2.mudawama.designsystem.size
import io.github.helmy2.mudawama.designsystem.utils.mudawamaButtonSemantics

@Composable
fun MudawamaPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .mudawamaButtonSemantics(contentDescription, enabled)
            .defaultMinSize(minHeight = MaterialTheme.size.buttonMinHeightLarge),
        enabled = enabled,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.default)) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.size(MaterialTheme.spacing.compact))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.size(MaterialTheme.spacing.compact))
                trailingIcon()
            }
        }
    }
}

