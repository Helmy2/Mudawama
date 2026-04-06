package io.github.helmy2.mudawama.habits.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import kotlinx.datetime.DayOfWeek
import mudawama.feature.habits.presentation.Res
import mudawama.feature.habits.presentation.action_delete_habit
import mudawama.feature.habits.presentation.action_delete_habit_subtitle
import mudawama.feature.habits.presentation.action_edit_habit
import mudawama.feature.habits.presentation.action_edit_habit_subtitle
import mudawama.feature.habits.presentation.action_manage_habit
import mudawama.feature.habits.presentation.action_reset_today
import mudawama.feature.habits.presentation.action_reset_today_subtitle
import org.jetbrains.compose.resources.stringResource

/**
 * "Manage Habit" options sheet — shown when the user taps ⋮ on a personal habit card.
 * Matches manage_habit_bottom_sheet.png exactly:
 *   - Drag handle at top
 *   - × close button (left) + "Manage Habit" title (centered)
 *   - Edit Habit row  (icon chip + title + subtitle + chevron)
 *   - Reset Today's Progress row
 *   - [divider]
 *   - Delete Habit row (red, non-core only)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitOptionsSheet(
    habit: Habit,
    onEdit: () -> Unit,
    onResetToday: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MudawamaTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
        ) {
            // ── Header row: × close | "Manage Habit" (centered) ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.action_manage_habit),
                    style = MudawamaTheme.typography.h3,
                    color = MudawamaTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.action_manage_habit),
                        tint = MudawamaTheme.colors.onSurface,
                    )
                }
            }

            // ── Action rows ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Edit Habit
                ManageActionRow(
                    icon = Icons.Default.Edit,
                    iconBackgroundColor = MudawamaTheme.colors.onSurface.copy(alpha = 0.08f),
                    iconTint = MudawamaTheme.colors.onSurface,
                    title = stringResource(Res.string.action_edit_habit),
                    subtitle = stringResource(Res.string.action_edit_habit_subtitle),
                    titleColor = MudawamaTheme.colors.onSurface,
                    onClick = onEdit,
                )

                // Reset Today's Progress
                ManageActionRow(
                    icon = Icons.Default.Refresh,
                    iconBackgroundColor = MudawamaTheme.colors.onSurface.copy(alpha = 0.08f),
                    iconTint = MudawamaTheme.colors.onSurface,
                    title = stringResource(Res.string.action_reset_today),
                    subtitle = stringResource(Res.string.action_reset_today_subtitle),
                    titleColor = MudawamaTheme.colors.onSurface,
                    onClick = onResetToday,
                )

                // Delete Habit (non-core only)
                if (!habit.isCore) {
                    HorizontalDivider(
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    ManageActionRow(
                        icon = Icons.Default.Delete,
                        iconBackgroundColor = MudawamaTheme.colors.error.copy(alpha = 0.12f),
                        iconTint = MudawamaTheme.colors.error,
                        title = stringResource(Res.string.action_delete_habit),
                        subtitle = stringResource(Res.string.action_delete_habit_subtitle),
                        titleColor = MudawamaTheme.colors.error,
                        onClick = onDelete,
                    )
                }
            }
        }
    }
}

/**
 * Single action row in the Manage Habit sheet.
 * Layout: [icon chip] | title + subtitle | [chevron]
 */
@Composable
private fun ManageActionRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MudawamaTheme.colors.onSurface.copy(alpha = 0.04f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon chip
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        // Title + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MudawamaTheme.typography.h4,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MudawamaTheme.typography.body2,
                color = titleColor.copy(alpha = 0.65f),
            )
        }

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MudawamaTheme.colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HabitOptionsSheetCustomPreview() {
    MudawamaTheme {
        HabitOptionsSheet(
            habit = Habit(
                id = "1", name = "Fasting Mondays", iconKey = "moon",
                type = HabitType.BOOLEAN, category = "custom",
                frequencyDays = DayOfWeek.entries.toSet(),
                isCore = false, goalCount = null, createdAt = 0L,
            ),
            onEdit = {}, onResetToday = {}, onDelete = {}, onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitOptionsSheetCorePreview() {
    MudawamaTheme {
        HabitOptionsSheet(
            habit = Habit(
                id = "2", name = "Fajr Prayer", iconKey = "pray",
                type = HabitType.BOOLEAN, category = "PRAYER",
                frequencyDays = DayOfWeek.entries.toSet(),
                isCore = true, goalCount = null, createdAt = 0L,
            ),
            onEdit = {}, onResetToday = {}, onDelete = {}, onDismiss = {},
        )
    }
}
