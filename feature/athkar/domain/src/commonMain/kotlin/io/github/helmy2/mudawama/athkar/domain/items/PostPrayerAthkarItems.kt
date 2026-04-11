package io.github.helmy2.mudawama.athkar.domain.items

import io.github.helmy2.mudawama.athkar.domain.model.AthkarItem

/**
 * Canonical Post-Prayer Athkar item list (Tasbeeh al-Fatima + key post-prayer supplications).
 * IDs are stable across app versions — do NOT change them once shipped.
 * Display strings (transliteration / translation) live in the presentation layer.
 */
val postPrayerAthkarItems: List<AthkarItem> = listOf(
    AthkarItem(id = "post_prayer_istighfar",      targetCount = 3),
    AthkarItem(id = "post_prayer_allahumma_anta", targetCount = 1),
    AthkarItem(id = "post_prayer_ayat_kursi",     targetCount = 1),
    AthkarItem(id = "post_prayer_subhanallah",    targetCount = 33),
    AthkarItem(id = "post_prayer_alhamdulillah",  targetCount = 33),
    AthkarItem(id = "post_prayer_allahu_akbar",   targetCount = 34),
    AthkarItem(id = "post_prayer_la_ilaha",       targetCount = 1),
)
