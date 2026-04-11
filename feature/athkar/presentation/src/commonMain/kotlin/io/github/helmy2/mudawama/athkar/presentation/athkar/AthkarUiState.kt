package io.github.helmy2.mudawama.athkar.presentation.athkar

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroup
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarItem
import io.github.helmy2.mudawama.athkar.domain.model.AthkarPrayerSlot

// ── State ────────────────────────────────────────────────────────────────────

data class AthkarUiState(
    val today: String = "",
    /** Completion status per group for today. */
    val completionStatus: Map<AthkarGroupType, Boolean> = emptyMap(),
    /** Per-item current counts for ALL groups (type → key → count). Used on overview. */
    val allGroupCounters: Map<AthkarGroupType, Map<String, Int>> = emptyMap(),
    /** The group currently open in the detail view, null = overview. */
    val activeGroup: AthkarGroup? = null,
    /** Per-item current counts for the active group (key → count). */
    val activeGroupCounters: Map<String, Int> = emptyMap(),
    /** Active prayer slot when [activeGroup] is POST_PRAYER. Defaults to FAJR. */
    val activePrayerSlot: AthkarPrayerSlot = AthkarPrayerSlot.FAJR,
    /** True while the active group log is loading. */
    val isLoading: Boolean = false,
) {
    val activeGroupIsComplete: Boolean
        get() = activeGroup?.let { completionStatus[it.type] } ?: false

    /**
     * Returns how many items in [type] have reached their target count today.
     * For POST_PRAYER: counts how many of the 5 prayer slots are fully done.
     * For MORNING/EVENING: counts items that reached their target.
     */
    fun completedCountFor(type: AthkarGroupType, items: List<AthkarItem>): Int {
        val counters = allGroupCounters[type] ?: return 0
        return if (type == AthkarGroupType.POST_PRAYER) {
            // Count completed prayer slots (a slot is done when ALL items hit target)
            AthkarPrayerSlot.all.count { slot ->
                items.all { item ->
                    val key = AthkarPrayerSlot.counterKey(item.id, slot)
                    (counters[key] ?: 0) >= item.targetCount
                }
            }
        } else {
            items.count { item -> (counters[item.id] ?: 0) >= item.targetCount }
        }
    }

    /**
     * For the current [activePrayerSlot], returns the counter value for [itemId].
     * For non-POST_PRAYER groups, delegates to [activeGroupCounters] with plain itemId.
     */
    fun currentCountFor(itemId: String): Int {
        return if (activeGroup?.type == AthkarGroupType.POST_PRAYER) {
            val key = AthkarPrayerSlot.counterKey(itemId, activePrayerSlot)
            activeGroupCounters[key] ?: 0
        } else {
            activeGroupCounters[itemId] ?: 0
        }
    }

    /**
     * Returns true if the current [activePrayerSlot] is fully complete for all [items].
     */
    fun currentSlotIsComplete(items: List<AthkarItem>): Boolean {
        if (activeGroup?.type != AthkarGroupType.POST_PRAYER) return activeGroupIsComplete
        return items.all { item ->
            val key = AthkarPrayerSlot.counterKey(item.id, activePrayerSlot)
            (activeGroupCounters[key] ?: 0) >= item.targetCount
        }
    }
}

// ── Actions ──────────────────────────────────────────────────────────────────

sealed interface AthkarUiAction {
    data class OpenGroup(val type: AthkarGroupType) : AthkarUiAction
    data object CloseGroup : AthkarUiAction
    data class IncrementItem(val itemId: String) : AthkarUiAction
    /** Long-press on a completed item resets its counter to 0. */
    data class ResetItem(val itemId: String) : AthkarUiAction
    /** Select which prayer slot to view/count in the POST_PRAYER detail screen. */
    data class SelectPrayerSlot(val slot: AthkarPrayerSlot) : AthkarUiAction
}

// ── Events ───────────────────────────────────────────────────────────────────

sealed interface AthkarUiEvent {
    data class GroupCompleted(val type: AthkarGroupType) : AthkarUiEvent
}
