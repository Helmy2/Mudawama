package io.github.helmy2.mudawama.athkar.domain.model

/**
 * A collection of [AthkarItem]s for a given [AthkarGroupType].
 */
data class AthkarGroup(
    val type: AthkarGroupType,
    val items: List<AthkarItem>,
)
