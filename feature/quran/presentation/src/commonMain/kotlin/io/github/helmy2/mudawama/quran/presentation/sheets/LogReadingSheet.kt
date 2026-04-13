package io.github.helmy2.mudawama.quran.presentation.sheets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.helmy2.mudawama.designsystem.components.MudawamaBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.quran.domain.error.MAX_SESSION_PAGES
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_log_reading_chip_five_pages
import mudawama.shared.designsystem.quran_log_reading_chip_one_juz
import mudawama.shared.designsystem.quran_log_reading_chip_one_page
import mudawama.shared.designsystem.quran_log_reading_done_button
import mudawama.shared.designsystem.quran_log_reading_session_label
import mudawama.shared.designsystem.quran_log_reading_sheet_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogReadingSheet(
    pageInput: Int,
    goalPages: Int,
    onPageInputChange: (Int) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (goalPages > 0) (pageInput / goalPages.toFloat()).coerceIn(0f, 1f) else 0f

    MudawamaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title aligned left
            Text(
                text = stringResource(Res.string.quran_log_reading_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // Mini progress arc + stepper centred
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    strokeWidth = 8.dp,
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                )
                Text(
                    text = pageInput.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.quran_log_reading_session_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(16.dp))

            // Stepper row with outlined circle buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (pageInput > 0) onPageInputChange(pageInput - 1) },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = pageInput.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )

                IconButton(
                    onClick = {
                        if (pageInput < MAX_SESSION_PAGES) onPageInputChange(pageInput + 1)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick-add chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    stringResource(Res.string.quran_log_reading_chip_one_page) to 1,
                    stringResource(Res.string.quran_log_reading_chip_five_pages) to 5,
                    stringResource(Res.string.quran_log_reading_chip_one_juz) to 20,
                ).forEach { (label, delta) ->
                    FilterChip(
                        selected = false,
                        onClick = { onPageInputChange((pageInput + delta).coerceAtMost(MAX_SESSION_PAGES)) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.primary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { if (pageInput > 0) onConfirm(pageInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = pageInput > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.quran_log_reading_done_button),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LogReadingSheetPreview() {
    LogReadingSheet(
        pageInput = 3,
        goalPages = 5,
        onPageInputChange = {},
        onConfirm = {},
        onDismiss = {},
    )
}
