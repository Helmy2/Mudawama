# Phase 1: Presentation Composables (Prayer Tracking Screen)

**Branch**: `006-prayer-screen` | **Date**: 2026-04-08 | **Spec**: [../spec.md](../spec.md)

---

## Overview

This document specifies the key Composable functions for the `feature:prayer:presentation` module. All UI must match the reference design `docs/ui/daily_prayer_tracker.png`.

## 1. Top-Level Screen

Location: `feature/prayer/presentation/src/commonMain/.../prayer/presentation/PrayerScreen.kt`

```kotlin
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = koinViewModel()
)

@Composable
internal fun PrayerScreenContent(
    state: PrayerUiState,
    onAction: (PrayerUiAction) -> Unit,
    modifier: Modifier = Modifier
)
```

## 2. Components

Location: `feature/prayer/presentation/src/commonMain/.../prayer/presentation/components/`

### `PrayerDateStrip`
```kotlin
/**
 * A horizontal strip showing 7 dates (3 past, today, 3 future).
 * The selected date is highlighted.
 */
@Composable
internal fun PrayerDateStrip(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)
```

### `PrayerCompletionHero`
```kotlin
/**
 * Shows the summary of completed prayers for the selected date.
 * E.g. "3/5 Prayers Completed". Shows an encouraging message or progress bar.
 */
@Composable
internal fun PrayerCompletionHero(
    prayers: List<PrayerWithStatus>,
    modifier: Modifier = Modifier
)
```

### `PrayerRowItem`
```kotlin
/**
 * Displays a single prayer row with its name, scheduled time, and a toggle button/status indicator.
 * Supports a long-press gesture to show the "Mark as Missed" action sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PrayerRowItem(
    prayer: PrayerWithStatus,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
)
```

### `MarkMissedBottomSheet`
```kotlin
/**
 * A bottom sheet that prompts the user to mark a specific prayer as MISSED.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarkMissedBottomSheet(
    prayer: PrayerWithStatus,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

---

## 3. Previews

Location: `feature/prayer/presentation/src/androidMain/.../prayer/presentation/PrayerScreenPreviews.kt`

```kotlin
@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview_AllPending() { ... }

@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview_MixedStatus() { ... }

@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview_Loading() { ... }
```
