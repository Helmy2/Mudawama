package io.github.helmy2.mudawama.athkar.domain.items

import io.github.helmy2.mudawama.athkar.domain.model.AthkarItem

/**
 * Canonical Morning Athkar item list.
 * IDs are stable across app versions — do NOT change them once shipped.
 * Display strings (transliteration / translation) live in the presentation layer.
 */
val morningAthkarItems: List<AthkarItem> = listOf(
    AthkarItem(id = "morning_isti",              targetCount = 1),
    AthkarItem(id = "morning_ayat_kursi",        targetCount = 1),
    AthkarItem(id = "morning_ikhlas",            targetCount = 3),
    AthkarItem(id = "morning_falaq",             targetCount = 3),
    AthkarItem(id = "morning_nas",               targetCount = 3),
    AthkarItem(id = "morning_subhanallah",       targetCount = 33),
    AthkarItem(id = "morning_alhamdulillah",     targetCount = 33),
    AthkarItem(id = "morning_allahu_akbar",      targetCount = 34),
    AthkarItem(id = "morning_sayyid_istighfar",  targetCount = 1),
    AthkarItem(id = "morning_protection",        targetCount = 1),
)
