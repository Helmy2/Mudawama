package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.items.eveningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.morningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.postPrayerAthkarItems
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroup
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType

/**
 * Returns the static [AthkarGroup] (items list) for the given [AthkarGroupType].
 * This is a pure function with no IO — it always returns the same result.
 */
class GetAthkarGroupUseCase {
    operator fun invoke(type: AthkarGroupType): AthkarGroup = AthkarGroup(
        type = type,
        items = when (type) {
            AthkarGroupType.MORNING -> morningAthkarItems
            AthkarGroupType.EVENING -> eveningAthkarItems
            AthkarGroupType.POST_PRAYER -> postPrayerAthkarItems
        },
    )
}
