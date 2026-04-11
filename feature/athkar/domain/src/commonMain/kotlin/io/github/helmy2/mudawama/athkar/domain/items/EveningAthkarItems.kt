package io.github.helmy2.mudawama.athkar.domain.items

import io.github.helmy2.mudawama.athkar.domain.model.AthkarItem

/**
 * Canonical Evening Athkar item list.
 * IDs are stable across app versions — do NOT change them once shipped.
 * Display strings (transliteration / translation) live in the presentation layer.
 */
val eveningAthkarItems: List<AthkarItem> = listOf(
    AthkarItem(id = "evening_isti",              targetCount = 1),
    AthkarItem(id = "evening_ayat_kursi",        targetCount = 1),
    AthkarItem(id = "evening_ikhlas",            targetCount = 3),
    AthkarItem(id = "evening_falaq",             targetCount = 3),
    AthkarItem(id = "evening_nas",               targetCount = 3),
    AthkarItem(id = "evening_subhanallah",       targetCount = 33),
    AthkarItem(id = "evening_alhamdulillah",     targetCount = 33),
    AthkarItem(id = "evening_allahu_akbar",      targetCount = 34),
    AthkarItem(id = "evening_sayyid_istighfar",  targetCount = 1),
    AthkarItem(id = "evening_protection",        targetCount = 1),
)
