package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.presentation.component.AthkarGroupCard
import io.github.helmy2.mudawama.athkar.presentation.component.displayItemsFor
import io.github.helmy2.mudawama.core.presentation.util.ObserveAsEvents
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AthkarScreen(
    viewModel: AthkarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.eventFlow) { /* GroupCompleted handled via state */ }

    RequestNotificationPermissionEffect()

    if (state.activeGroup != null) {
        AthkarGroupScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    } else {
        AthkarOverviewContent(
            state = state,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
internal fun AthkarOverviewContent(
    state: AthkarUiState,
    onAction: (AthkarUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.athkar_screen_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.today,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AthkarGroupType.entries.forEach { type ->
                val displayItems = displayItemsFor(type)
                val domainItems = displayItems.map { it.item }
                val totalCount = if (type == AthkarGroupType.POST_PRAYER) 5 else domainItems.size
                AthkarGroupCard(
                    type = type,
                    isComplete = state.completionStatus[type] ?: false,
                    completedCount = state.completedCountFor(type, domainItems),
                    totalCount = totalCount,
                    onTap = { onAction(AthkarUiAction.OpenGroup(type)) },
                )
            }

            Spacer(Modifier.height(96.dp))
        }
    }
}