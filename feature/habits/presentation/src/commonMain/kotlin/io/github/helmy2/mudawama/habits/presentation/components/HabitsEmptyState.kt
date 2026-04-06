package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.empty_habits_body
import mudawama.feature.habits.presentation.empty_habits_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun HabitsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.empty_habits_title),
            style = MudawamaTheme.typography.h2,
            color = MudawamaTheme.colors.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.empty_habits_body),
            style = MudawamaTheme.typography.body1,
            color = MudawamaTheme.colors.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitsEmptyStatePreview() {
    MudawamaTheme {
        HabitsEmptyState()
    }
}
