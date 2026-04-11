package io.github.helmy2.mudawama.athkar.domain.model

/**
 * Represents one of the 5 daily prayers for post-prayer Athkar tracking.
 * The [index] (0–4) is appended to counter keys: `"itemId#index"`.
 */
enum class AthkarPrayerSlot(val index: Int) {
    FAJR(0),
    DHUHR(1),
    ASR(2),
    MAGHRIB(3),
    ISHA(4);

    companion object {
        val all: List<AthkarPrayerSlot> = entries

        /** Compose a counter key for a given item and prayer slot. */
        fun counterKey(itemId: String, slot: AthkarPrayerSlot): String = "$itemId#${slot.index}"

        /** Parse a counter key back into itemId and slot index. */
        fun parseKey(key: String): Pair<String, Int>? {
            val parts = key.split("#")
            if (parts.size != 2) return null
            val idx = parts[1].toIntOrNull() ?: return null
            return parts[0] to idx
        }
    }
}
