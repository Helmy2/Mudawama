# UI Contracts: 008-athkar-tasbeeh

Generated during `/speckit.plan` for branch `008-athkar-tasbeeh`.

This document defines the contracts between the `:presentation` layer and the `:domain` layer — specifically the MVI State/Action/Event shapes for each ViewModel, and the Composable slot APIs for shared components.

---

## AthkarViewModel

Drives `AthkarScreen` (the Daily Athkar overview) and `AthkarGroupScreen` (the per-group session).

### State

```kotlin
data class AthkarState(
    val todayDate: String = "",                         // "yyyy-MM-dd"
    val morningComplete: Boolean = false,
    val eveningComplete: Boolean = false,
    val postPrayerComplete: Boolean = false,
    val activeGroup: AthkarGroupType? = null,           // null = overview screen
    val activeGroupItems: List<AthkarItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,                   // transient; cleared after display
)

data class AthkarItemUiModel(
    val id: String,
    val transliterationKey: String,   // Res.string key
    val translationKey: String,       // Res.string key
    val currentCount: Int,
    val targetCount: Int,
    val isComplete: Boolean,          // currentCount >= targetCount
)
```

### Action

```kotlin
sealed interface AthkarAction {
    data class OpenGroup(val groupType: AthkarGroupType) : AthkarAction
    data object CloseGroup : AthkarAction
    data class IncrementItem(val itemId: String) : AthkarAction
}
```

### Event

```kotlin
sealed interface AthkarEvent {
    data class GroupCompleted(val groupType: AthkarGroupType) : AthkarEvent  // trigger celebration UI
    data object ShowError : AthkarEvent
}
```

---

## TasbeehViewModel

Drives `TasbeehScreen` and `TasbeehGoalBottomSheet`.

### State

```kotlin
data class TasbeehState(
    val sessionCount: Int = 0,
    val dailyTotal: Int = 0,
    val goal: Int = 100,
    val isGoalReached: Boolean = false,        // true once per session; cleared on reset
    val isGoalSheetVisible: Boolean = false,
    val goalInputValue: String = "",           // transient text field state
)
```

### Action

```kotlin
sealed interface TasbeehAction {
    data object Tap : TasbeehAction
    data object Reset : TasbeehAction
    data object OpenGoalSheet : TasbeehAction
    data object CloseGoalSheet : TasbeehAction
    data class UpdateGoalInput(val value: String) : TasbeehAction
    data object ConfirmGoal : TasbeehAction
    data class SelectPresetGoal(val count: Int) : TasbeehAction   // 33, 100, 300
}
```

### Event

```kotlin
sealed interface TasbeehEvent {
    data object GoalReached : TasbeehEvent        // trigger completion haptic
    data object TapHaptic : TasbeehEvent          // trigger short tap haptic on every increment
    data object InvalidGoalInput : TasbeehEvent   // goal input was not a valid positive integer
}
```

---

## AthkarNotificationViewModel

Drives the notification preference rows inside the Settings screen.

### State

```kotlin
data class AthkarNotificationState(
    val morningEnabled: Boolean = false,
    val morningHour: Int = 6,
    val morningMinute: Int = 0,
    val eveningEnabled: Boolean = false,
    val eveningHour: Int = 18,
    val eveningMinute: Int = 0,
    val permissionStatus: NotificationPermissionStatus = NotificationPermissionStatus.UNKNOWN,
)

enum class NotificationPermissionStatus { UNKNOWN, GRANTED, DENIED, NEEDS_RATIONALE }
```

### Action

```kotlin
sealed interface AthkarNotificationAction {
    data class SetMorningEnabled(val enabled: Boolean) : AthkarNotificationAction
    data class SetMorningTime(val hour: Int, val minute: Int) : AthkarNotificationAction
    data class SetEveningEnabled(val enabled: Boolean) : AthkarNotificationAction
    data class SetEveningTime(val hour: Int, val minute: Int) : AthkarNotificationAction
    data object RequestPermission : AthkarNotificationAction
    data object RefreshPermissionStatus : AthkarNotificationAction
}
```

### Event

```kotlin
sealed interface AthkarNotificationEvent {
    data object OpenSystemSettings : AthkarNotificationEvent   // permission permanently denied
    data object PermissionGranted : AthkarNotificationEvent
    data object PermissionDenied : AthkarNotificationEvent
}
```

---

## Repository Interfaces (`feature:athkar:domain`)

### AthkarRepository

```kotlin
interface AthkarRepository {
    fun observeLog(groupType: AthkarGroupType, date: String): Flow<AthkarDailyLog?>
    fun observeCompletionStatus(date: String): Flow<Map<AthkarGroupType, Boolean>>
    /**
     * Increments the counter for [itemId] within [groupType] for [date].
     * The implementation resolves the item's targetCount directly from the static
     * items list (MorningAthkarItems / EveningAthkarItems / PostPrayerAthkarItems)
     * by itemId — targetCount is NOT passed by the caller. Clamping at targetCount
     * (FR-007b) is enforced here, not in the ViewModel.
     */
    suspend fun incrementItem(groupType: AthkarGroupType, date: String, itemId: String): EmptyResult<AthkarError>
}
```

### TasbeehRepository

```kotlin
interface TasbeehRepository {
    fun observeGoal(): Flow<TasbeehGoal>
    suspend fun setGoal(goalCount: Int): EmptyResult<AthkarError>
    fun observeDailyTotal(date: String): Flow<TasbeehDailyTotal>
    suspend fun addToDaily(date: String, amount: Int): EmptyResult<AthkarError>
}
```

### AthkarNotificationRepository (`shared:core:notifications` or `feature:athkar:data`)

```kotlin
interface AthkarNotificationRepository {
    fun observePreference(groupType: AthkarGroupType): Flow<NotificationPreference>
    suspend fun savePreference(preference: NotificationPreference): EmptyResult<AthkarError>
}
```

---

## NotificationScheduler Interface (`shared:core:notifications`)

```kotlin
interface NotificationScheduler {
    /**
     * Schedules (or re-schedules) a daily notification for the given group.
     * Cancels any previously scheduled notification with the same [notificationId].
     *
     * [title] and [body] are already-resolved strings. The caller
     * (AthkarNotificationRepositoryImpl in feature:athkar:data) is responsible
     * for resolving Res.string.* keys before invoking this method.
     * shared:core:notifications has no dependency on shared:designsystem and
     * cannot call stringResource().
     */
    suspend fun scheduleDailyReminder(
        notificationId: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
    )

    /** Cancels a previously scheduled notification. No-op if not scheduled. */
    suspend fun cancelReminder(notificationId: Int)
}

interface NotificationPermissionChecker {
    fun hasPermission(): Boolean
    /** Triggers the platform permission request. Result is delivered via callback/event. */
    suspend fun requestPermission(): NotificationPermissionResult
}

enum class NotificationPermissionResult { GRANTED, DENIED, NEEDS_RATIONALE }
```

**Notification IDs** (stable constants in `feature:athkar:domain`):

```kotlin
object AthkarNotificationIds {
    const val MORNING = 1001
    const val EVENING = 1002
}
```

---

## Domain Error Type (`feature:athkar:domain`)

```kotlin
sealed interface AthkarError : DomainError {
    data object DatabaseError : AthkarError
    data object NotificationSchedulingError : AthkarError
    data object InvalidInput : AthkarError
}
```

---

## Composable Screen Slots

### AthkarScreen (overview)

```
AthkarScreen(
    state: AthkarState,
    onAction: (AthkarAction) -> Unit
)
```

Renders three `AthkarGroupCard` composables (Morning, Evening, Post-Prayer), each showing group name, completion ring, and completion/incomplete badge.

### AthkarGroupScreen (session)

```
AthkarGroupScreen(
    state: AthkarState,          // activeGroup + activeGroupItems populated
    onAction: (AthkarAction) -> Unit
)
```

Renders per-item `AthkarItemCard` composables (transliteration label, count display, TAP TO COUNT button). Group-level completion animation fires on `AthkarEvent.GroupCompleted`.

### TasbeehScreen

```
TasbeehScreen(
    state: TasbeehState,
    onAction: (TasbeehAction) -> Unit
)
```

Large circular tap target, progress arc, reset/goal buttons below, TODAY'S TOTAL + CURRENT SESSION stats row. `TasbeehGoalBottomSheet` shown when `state.isGoalSheetVisible`.

### TasbeehGoalBottomSheet

```
TasbeehGoalBottomSheet(
    state: TasbeehState,
    onAction: (TasbeehAction) -> Unit
)
```

Uses `MudawamaBottomSheet` wrapper. Preset tiles (33, 100, 300) + custom numeric TextField + confirm button.
