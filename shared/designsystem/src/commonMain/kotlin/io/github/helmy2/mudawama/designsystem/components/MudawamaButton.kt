package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.utils.mudawamaButtonSemantics

@Composable
fun MudawamaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    val colors = ButtonDefaults.buttonColors(
        containerColor = MudawamaTheme.colors.primary,
        contentColor = MudawamaTheme.colors.onPrimary
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .mudawamaButtonSemantics(contentDescription, enabled)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 0.dp),
        enabled = enabled,
        colors = colors,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (leadingIcon != null) {
                leadingIcon()
                // spacing
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = text, style = MudawamaTheme.typography.button)
            if (trailingIcon != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                trailingIcon()
            }
        }
    }
}

