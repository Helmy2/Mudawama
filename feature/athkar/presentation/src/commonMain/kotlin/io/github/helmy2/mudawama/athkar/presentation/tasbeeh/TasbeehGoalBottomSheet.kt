package io.github.helmy2.mudawama.athkar.presentation.tasbeeh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.components.MudawamaBottomSheet
import io.github.helmy2.mudawama.designsystem.components.MudawamaPrimaryButton
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.tasbeeh_goal_confirm_button
import mudawama.shared.designsystem.tasbeeh_goal_custom_hint
import mudawama.shared.designsystem.tasbeeh_goal_preset_100
import mudawama.shared.designsystem.tasbeeh_goal_preset_300
import mudawama.shared.designsystem.tasbeeh_goal_preset_33
import mudawama.shared.designsystem.tasbeeh_goal_sheet_title
import mudawama.shared.designsystem.tasbeeh_error_invalid_goal
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasbeehGoalBottomSheet(
    state: TasbeehUiState,
    onAction: (TasbeehUiAction) -> Unit,
) {
    MudawamaBottomSheet(
        onDismissRequest = { onAction(TasbeehUiAction.CloseGoalSheet) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.tasbeeh_goal_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    stringResource(Res.string.tasbeeh_goal_preset_33) to 33,
                    stringResource(Res.string.tasbeeh_goal_preset_100) to 100,
                    stringResource(Res.string.tasbeeh_goal_preset_300) to 300,
                ).forEach { (label, count) ->
                    FilterChip(
                        selected = state.goalCount == count && state.goalInputValue == count.toString(),
                        onClick = { onAction(TasbeehUiAction.SelectPresetGoal(count)) },
                        label = { Text(label) },
                    )
                }
            }

            OutlinedTextField(
                value = state.goalInputValue,
                onValueChange = { onAction(TasbeehUiAction.UpdateGoalInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.tasbeeh_goal_custom_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = state.goalInputError,
                supportingText = if (state.goalInputError) {
                    { Text(stringResource(Res.string.tasbeeh_error_invalid_goal)) }
                } else null,
            )

            MudawamaPrimaryButton(
                text = stringResource(Res.string.tasbeeh_goal_confirm_button),
                onClick = { onAction(TasbeehUiAction.ConfirmGoal) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
