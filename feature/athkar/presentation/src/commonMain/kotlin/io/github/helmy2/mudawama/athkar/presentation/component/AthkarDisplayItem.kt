package io.github.helmy2.mudawama.athkar.presentation.component

import io.github.helmy2.mudawama.athkar.domain.model.AthkarItem
import org.jetbrains.compose.resources.StringResource

/**
 * Presentation-layer pairing of a domain [AthkarItem] with its display strings.
 * [transliteration] and [translation] are resolved [StringResource] values — never raw keys.
 */
data class AthkarDisplayItem(
    val item: AthkarItem,
    val transliteration: StringResource,
    val translation: StringResource,
)
