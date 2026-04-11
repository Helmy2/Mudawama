package io.github.helmy2.mudawama.athkar.data.repository

import io.github.helmy2.mudawama.athkar.data.mapper.toDomain
import io.github.helmy2.mudawama.athkar.data.mapper.toEntity
import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.items.eveningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.morningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.postPrayerAthkarItems
import io.github.helmy2.mudawama.athkar.domain.model.AthkarDailyLog
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarPrayerSlot
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarRepository
import io.github.helmy2.mudawama.core.database.dao.AthkarDailyLogDao
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class AthkarRepositoryImpl(
    private val dao: AthkarDailyLogDao,
    private val dispatcher: CoroutineDispatcher,
) : AthkarRepository {

    override fun observeLog(groupType: AthkarGroupType, date: String): Flow<AthkarDailyLog?> =
        dao.getLog(groupType.name, date).map { it?.toDomain() }

    override fun observeCompletionStatus(date: String): Flow<Map<AthkarGroupType, Boolean>> =
        dao.getCompletionStatusForDate(date).map { entities ->
            val result = mutableMapOf<AthkarGroupType, Boolean>()
            AthkarGroupType.entries.forEach { type -> result[type] = false }
            entities.forEach { entity ->
                runCatching { AthkarGroupType.valueOf(entity.groupType) }.getOrNull()
                    ?.let { type -> result[type] = entity.isComplete }
            }
            result
        }

    override suspend fun incrementItem(
        groupType: AthkarGroupType,
        date: String,
        itemId: String,
        prayerSlot: AthkarPrayerSlot?,
    ): EmptyResult<AthkarError> = withContext(dispatcher) {
        val items = itemsFor(groupType)
        val targetItem = items.find { it.id == itemId }
            ?: return@withContext Result.Success(Unit)

        val counterKey = resolveKey(groupType, itemId, prayerSlot)

        val currentLog = dao.getLog(groupType.name, date).first()
        val currentCounters = currentLog?.counters ?: emptyMap()
        val currentCount = currentCounters[counterKey] ?: 0

        if (currentCount >= targetItem.targetCount) {
            return@withContext Result.Success(Unit) // already at target — no-op
        }

        val newCounters = currentCounters.toMutableMap()
            .also { it[counterKey] = currentCount + 1 }

        val isComplete = computeIsComplete(groupType, newCounters)

        val updatedLog = AthkarDailyLog(
            groupType = groupType,
            date = date,
            counters = newCounters,
            isComplete = isComplete,
        )

        safeCall(
            block = { dao.upsertLog(updatedLog.toEntity()) },
            onError = { AthkarError.DatabaseError },
        )
    }

    override suspend fun resetItem(
        groupType: AthkarGroupType,
        date: String,
        itemId: String,
        prayerSlot: AthkarPrayerSlot?,
    ): EmptyResult<AthkarError> = withContext(dispatcher) {
        val counterKey = resolveKey(groupType, itemId, prayerSlot)

        val currentLog = dao.getLog(groupType.name, date).first()
        val currentCounters = currentLog?.counters ?: emptyMap()

        val newCounters = currentCounters.toMutableMap()
            .also { it[counterKey] = 0 }

        val isComplete = computeIsComplete(groupType, newCounters)

        val updatedLog = AthkarDailyLog(
            groupType = groupType,
            date = date,
            counters = newCounters,
            isComplete = isComplete,
        )

        safeCall(
            block = { dao.upsertLog(updatedLog.toEntity()) },
            onError = { AthkarError.DatabaseError },
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun itemsFor(groupType: AthkarGroupType) = when (groupType) {
        AthkarGroupType.MORNING     -> morningAthkarItems
        AthkarGroupType.EVENING     -> eveningAthkarItems
        AthkarGroupType.POST_PRAYER -> postPrayerAthkarItems
    }

    /**
     * For POST_PRAYER with a slot, the counter key is `"itemId#slotIndex"`.
     * For all other types (or POST_PRAYER without a slot), the key is just `itemId`.
     */
    private fun resolveKey(
        groupType: AthkarGroupType,
        itemId: String,
        prayerSlot: AthkarPrayerSlot?,
    ): String = if (groupType == AthkarGroupType.POST_PRAYER && prayerSlot != null) {
        AthkarPrayerSlot.counterKey(itemId, prayerSlot)
    } else {
        itemId
    }

    /**
     * For MORNING/EVENING: all items must reach targetCount.
     * For POST_PRAYER: all items across all 5 prayer slots must reach targetCount.
     */
    private fun computeIsComplete(
        groupType: AthkarGroupType,
        counters: Map<String, Int>,
    ): Boolean {
        val items = itemsFor(groupType)
        return if (groupType == AthkarGroupType.POST_PRAYER) {
            AthkarPrayerSlot.all.all { slot ->
                items.all { item ->
                    val key = AthkarPrayerSlot.counterKey(item.id, slot)
                    (counters[key] ?: 0) >= item.targetCount
                }
            }
        } else {
            items.all { item -> (counters[item.id] ?: 0) >= item.targetCount }
        }
    }
}
