package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ActionItem(
    val label: String,
    val icon: ImageVector? = null,
    val color: Color,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    title: String,
    actions: List<ActionItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MudawamaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            actions.forEach { action ->
                TextButton(
                    onClick = action.onClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    action.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = action.color,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = action.label,
                        color = action.color,
                    )
                }
            }
        }
    }
}