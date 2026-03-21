package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.designsystem.utils.mudawamaButtonSemantics

@Composable
fun MudawamaGhostButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    outlined: Boolean = false,
    contentDescription: String? = null
) {
    val contentColor = MudawamaTheme.colors.primary

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .mudawamaButtonSemantics(contentDescription, enabled)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.28f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
            shape = MaterialTheme.shapes.small
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(text = text, style = MudawamaTheme.typography.button)
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    trailingIcon()
                }
            }
        }
    } else {
        TextButton(
            onClick = onClick,
            modifier = modifier
                .mudawamaButtonSemantics(contentDescription, enabled)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            shape = MaterialTheme.shapes.small
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(text = text, style = MudawamaTheme.typography.button)
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

