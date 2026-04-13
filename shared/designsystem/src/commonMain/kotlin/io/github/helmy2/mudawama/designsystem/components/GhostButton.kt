package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.spacing
import io.github.helmy2.mudawama.designsystem.size
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
    val contentColor = MaterialTheme.colorScheme.primary

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .mudawamaButtonSemantics(contentDescription, enabled)
                .defaultMinSize(minHeight = MaterialTheme.size.buttonMinHeight),
            enabled = enabled,
            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.28f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
            shape = MaterialTheme.shapes.small,
        ) {
            Row(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default, vertical = MaterialTheme.spacing.compact)) {
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
    } else {
        TextButton(
            onClick = onClick,
            modifier = modifier
                .mudawamaButtonSemantics(contentDescription, enabled)
                .defaultMinSize(minHeight = MaterialTheme.size.buttonMinHeight),
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            shape = MaterialTheme.shapes.small,
        ) {
            Row(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default, vertical = MaterialTheme.spacing.compact)) {
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
}

