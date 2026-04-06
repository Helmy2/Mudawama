@file:OptIn(ExperimentalUuidApi::class)

package io.github.helmy2.mudawama.habits.domain.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates a random UUID v4 string via `kotlin.uuid.Uuid.random()`
 * (Kotlin 2.0+ stdlib — no external UUID dependency needed, Decision 1 in research.md).
 *
 * Marked `internal` so callers outside `:domain` cannot generate IDs directly.
 * Used by `CreateHabitUseCase`, `ToggleHabitCompletionUseCase`, and
 * `IncrementHabitCountUseCase`.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun generateId(): String = Uuid.random().toString()

