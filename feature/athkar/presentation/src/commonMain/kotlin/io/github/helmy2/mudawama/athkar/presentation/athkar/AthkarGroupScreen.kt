package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarPrayerSlot
import io.github.helmy2.mudawama.athkar.presentation.component.AthkarItemCard
import io.github.helmy2.mudawama.athkar.presentation.component.displayItemsFor
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_evening_title
import mudawama.shared.designsystem.athkar_group_complete
import mudawama.shared.designsystem.athkar_morning_title
import mudawama.shared.designsystem.athkar_post_prayer_title
import mudawama.shared.designsystem.athkar_prayer_slot_asr
import mudawama.shared.designsystem.athkar_prayer_slot_dhuhr
import mudawama.shared.designsystem.athkar_prayer_slot_fajr
import mudawama.shared.designsystem.athkar_prayer_slot_isha
import mudawama.shared.designsystem.athkar_prayer_slot_maghrib
import org.jetbrains.compose.resources.stringResource

/**
 * Detail screen showing all items for the currently open Athkar group.
 * - System back / back arrow: returns to overview.
 * - Long-press on a completed item: resets its counter to 0.
 * - POST_PRAYER only: shows a prayer slot tab row (Fajr … Isha).
 */
@Composable
fun AthkarGroupScreen(
    state: AthkarUiState,
    onAction: (AthkarUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val group = state.activeGroup ?: return
    val displayItems = displayItemsFor(group.type)
    val isPostPrayer = group.type == AthkarGroupType.POST_PRAYER

    val groupTitle = stringResource(
        when (group.type) {
            AthkarGroupType.MORNING -> Res.string.athkar_morning_title
            AthkarGroupType.EVENING -> Res.string.athkar_evening_title
            AthkarGroupType.POST_PRAYER -> Res.string.athkar_post_prayer_title
        }
    )

    // Intercept system back to close the group instead of exiting the app.
    AthkarBackHandler { onAction(AthkarUiAction.CloseGroup) }


    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Custom header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onAction(AthkarUiAction.CloseGroup) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Prayer slot selector (POST_PRAYER only) ────────────────────────
        if (isPostPrayer) {
            PrayerSlotRow(
                selected = state.activePrayerSlot,
                onSelect = { onAction(AthkarUiAction.SelectPrayerSlot(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }

        // ── Content ────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val slotComplete = state.currentSlotIsComplete(displayItems.map { it.item })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (slotComplete) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                text = stringResource(Res.string.athkar_group_complete),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                items(displayItems, key = { it.item.id }) { displayItem ->
                    val currentCount = state.currentCountFor(displayItem.item.id)
                    AthkarItemCard(
                        displayItem = displayItem,
                        currentCount = currentCount,
                        onTap = { onAction(AthkarUiAction.IncrementItem(displayItem.item.id)) },
                        onLongPress = { onAction(AthkarUiAction.ResetItem(displayItem.item.id)) },
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun PrayerSlotRow(
    selected: AthkarPrayerSlot,
    onSelect: (AthkarPrayerSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = mapOf(
        AthkarPrayerSlot.FAJR to stringResource(Res.string.athkar_prayer_slot_fajr),
        AthkarPrayerSlot.DHUHR to stringResource(Res.string.athkar_prayer_slot_dhuhr),
        AthkarPrayerSlot.ASR to stringResource(Res.string.athkar_prayer_slot_asr),
        AthkarPrayerSlot.MAGHRIB to stringResource(Res.string.athkar_prayer_slot_maghrib),
        AthkarPrayerSlot.ISHA to stringResource(Res.string.athkar_prayer_slot_isha),
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AthkarPrayerSlot.all.forEach { slot ->
            FilterChip(
                selected = slot == selected,
                onClick = { onSelect(slot) },
                label = {
                    Text(
                        text = labels[slot] ?: slot.name,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun AthkarGroupScreenPreview() {
    // Minimal preview stub
}
