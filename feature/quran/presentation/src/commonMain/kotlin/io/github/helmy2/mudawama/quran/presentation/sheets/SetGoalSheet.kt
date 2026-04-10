package io.github.helmy2.mudawama.quran.presentation.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.helmy2.mudawama.designsystem.components.MudawamaBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.quran.domain.error.MAX_DAILY_GOAL_PAGES
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_goal_sheet_five_pages
import mudawama.shared.designsystem.quran_goal_sheet_hint
import mudawama.shared.designsystem.quran_goal_sheet_one_juz
import mudawama.shared.designsystem.quran_goal_sheet_one_page
import mudawama.shared.designsystem.quran_goal_sheet_pages_per_day
import mudawama.shared.designsystem.quran_goal_sheet_popular_goals
import mudawama.shared.designsystem.quran_goal_sheet_save_button
import mudawama.shared.designsystem.quran_goal_sheet_ten_pages
import mudawama.shared.designsystem.quran_goal_sheet_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalSheet(
    currentGoal: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var goalInput by rememberSaveable { mutableIntStateOf(currentGoal) }

    // Sync local state when the upstream goal changes (e.g., sheet re-opened after save)
    LaunchedEffect(currentGoal) { goalInput = currentGoal }

    MudawamaBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(Res.string.quran_goal_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.quran_goal_sheet_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.quran_goal_sheet_pages_per_day),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            // Stepper row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (goalInput > 1) goalInput-- },
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Text(
                    text = goalInput.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = { if (goalInput < MAX_DAILY_GOAL_PAGES) goalInput++ },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.quran_goal_sheet_popular_goals),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            val popularGoals = listOf(
                1 to stringResource(Res.string.quran_goal_sheet_one_page),
                5 to stringResource(Res.string.quran_goal_sheet_five_pages),
                10 to stringResource(Res.string.quran_goal_sheet_ten_pages),
                20 to stringResource(Res.string.quran_goal_sheet_one_juz),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                popularGoals.forEach { (pages, label) ->
                    val isSelected = goalInput == pages
                    FilterChip(
                        selected = isSelected,
                        onClick = { goalInput = pages },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSave(goalInput) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.quran_goal_sheet_save_button))
            }
        }
    }
}

@Preview
@Composable
private fun SetGoalSheetPreview() {
    SetGoalSheet(
        currentGoal = 5,
        onSave = {},
        onDismiss = {},
    )
}
