package io.github.helmy2.mudawama.athkar.presentation.component

import io.github.helmy2.mudawama.athkar.domain.items.eveningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.morningAthkarItems
import io.github.helmy2.mudawama.athkar.domain.items.postPrayerAthkarItems
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.athkar_evening_alhamdulillah_transliteration
import mudawama.shared.designsystem.athkar_evening_alhamdulillah_translation
import mudawama.shared.designsystem.athkar_evening_allahu_akbar_transliteration
import mudawama.shared.designsystem.athkar_evening_allahu_akbar_translation
import mudawama.shared.designsystem.athkar_evening_ayat_kursi_transliteration
import mudawama.shared.designsystem.athkar_evening_ayat_kursi_translation
import mudawama.shared.designsystem.athkar_evening_falaq_transliteration
import mudawama.shared.designsystem.athkar_evening_falaq_translation
import mudawama.shared.designsystem.athkar_evening_ikhlas_transliteration
import mudawama.shared.designsystem.athkar_evening_ikhlas_translation
import mudawama.shared.designsystem.athkar_evening_isti_transliteration
import mudawama.shared.designsystem.athkar_evening_isti_translation
import mudawama.shared.designsystem.athkar_evening_nas_transliteration
import mudawama.shared.designsystem.athkar_evening_nas_translation
import mudawama.shared.designsystem.athkar_evening_protection_transliteration
import mudawama.shared.designsystem.athkar_evening_protection_translation
import mudawama.shared.designsystem.athkar_evening_sayyid_istighfar_transliteration
import mudawama.shared.designsystem.athkar_evening_sayyid_istighfar_translation
import mudawama.shared.designsystem.athkar_evening_subhanallah_transliteration
import mudawama.shared.designsystem.athkar_evening_subhanallah_translation
import mudawama.shared.designsystem.athkar_morning_alhamdulillah_transliteration
import mudawama.shared.designsystem.athkar_morning_alhamdulillah_translation
import mudawama.shared.designsystem.athkar_morning_allahu_akbar_transliteration
import mudawama.shared.designsystem.athkar_morning_allahu_akbar_translation
import mudawama.shared.designsystem.athkar_morning_ayat_kursi_transliteration
import mudawama.shared.designsystem.athkar_morning_ayat_kursi_translation
import mudawama.shared.designsystem.athkar_morning_falaq_transliteration
import mudawama.shared.designsystem.athkar_morning_falaq_translation
import mudawama.shared.designsystem.athkar_morning_ikhlas_transliteration
import mudawama.shared.designsystem.athkar_morning_ikhlas_translation
import mudawama.shared.designsystem.athkar_morning_isti_transliteration
import mudawama.shared.designsystem.athkar_morning_isti_translation
import mudawama.shared.designsystem.athkar_morning_nas_transliteration
import mudawama.shared.designsystem.athkar_morning_nas_translation
import mudawama.shared.designsystem.athkar_morning_protection_transliteration
import mudawama.shared.designsystem.athkar_morning_protection_translation
import mudawama.shared.designsystem.athkar_morning_sayyid_istighfar_transliteration
import mudawama.shared.designsystem.athkar_morning_sayyid_istighfar_translation
import mudawama.shared.designsystem.athkar_morning_subhanallah_transliteration
import mudawama.shared.designsystem.athkar_morning_subhanallah_translation
import mudawama.shared.designsystem.athkar_post_prayer_alhamdulillah_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_alhamdulillah_translation
import mudawama.shared.designsystem.athkar_post_prayer_allahu_akbar_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_allahu_akbar_translation
import mudawama.shared.designsystem.athkar_post_prayer_allahumma_anta_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_allahumma_anta_translation
import mudawama.shared.designsystem.athkar_post_prayer_ayat_kursi_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_ayat_kursi_translation
import mudawama.shared.designsystem.athkar_post_prayer_istighfar_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_istighfar_translation
import mudawama.shared.designsystem.athkar_post_prayer_la_ilaha_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_la_ilaha_translation
import mudawama.shared.designsystem.athkar_post_prayer_subhanallah_transliteration
import mudawama.shared.designsystem.athkar_post_prayer_subhanallah_translation

/** Presentation-layer display list for Morning Athkar, zipped with their [StringResource]s. */
val morningDisplayItems: List<AthkarDisplayItem> = morningAthkarItems.zip(
    listOf(
        Res.string.athkar_morning_isti_transliteration        to Res.string.athkar_morning_isti_translation,
        Res.string.athkar_morning_ayat_kursi_transliteration  to Res.string.athkar_morning_ayat_kursi_translation,
        Res.string.athkar_morning_ikhlas_transliteration      to Res.string.athkar_morning_ikhlas_translation,
        Res.string.athkar_morning_falaq_transliteration       to Res.string.athkar_morning_falaq_translation,
        Res.string.athkar_morning_nas_transliteration         to Res.string.athkar_morning_nas_translation,
        Res.string.athkar_morning_subhanallah_transliteration to Res.string.athkar_morning_subhanallah_translation,
        Res.string.athkar_morning_alhamdulillah_transliteration to Res.string.athkar_morning_alhamdulillah_translation,
        Res.string.athkar_morning_allahu_akbar_transliteration to Res.string.athkar_morning_allahu_akbar_translation,
        Res.string.athkar_morning_sayyid_istighfar_transliteration to Res.string.athkar_morning_sayyid_istighfar_translation,
        Res.string.athkar_morning_protection_transliteration  to Res.string.athkar_morning_protection_translation,
    )
) { item, (trans, transl) -> AthkarDisplayItem(item, trans, transl) }

/** Presentation-layer display list for Evening Athkar, zipped with their [StringResource]s. */
val eveningDisplayItems: List<AthkarDisplayItem> = eveningAthkarItems.zip(
    listOf(
        Res.string.athkar_evening_isti_transliteration        to Res.string.athkar_evening_isti_translation,
        Res.string.athkar_evening_ayat_kursi_transliteration  to Res.string.athkar_evening_ayat_kursi_translation,
        Res.string.athkar_evening_ikhlas_transliteration      to Res.string.athkar_evening_ikhlas_translation,
        Res.string.athkar_evening_falaq_transliteration       to Res.string.athkar_evening_falaq_translation,
        Res.string.athkar_evening_nas_transliteration         to Res.string.athkar_evening_nas_translation,
        Res.string.athkar_evening_subhanallah_transliteration to Res.string.athkar_evening_subhanallah_translation,
        Res.string.athkar_evening_alhamdulillah_transliteration to Res.string.athkar_evening_alhamdulillah_translation,
        Res.string.athkar_evening_allahu_akbar_transliteration to Res.string.athkar_evening_allahu_akbar_translation,
        Res.string.athkar_evening_sayyid_istighfar_transliteration to Res.string.athkar_evening_sayyid_istighfar_translation,
        Res.string.athkar_evening_protection_transliteration  to Res.string.athkar_evening_protection_translation,
    )
) { item, (trans, transl) -> AthkarDisplayItem(item, trans, transl) }

/** Presentation-layer display list for Post-Prayer Athkar, zipped with their [StringResource]s. */
val postPrayerDisplayItems: List<AthkarDisplayItem> = postPrayerAthkarItems.zip(
    listOf(
        Res.string.athkar_post_prayer_istighfar_transliteration      to Res.string.athkar_post_prayer_istighfar_translation,
        Res.string.athkar_post_prayer_allahumma_anta_transliteration to Res.string.athkar_post_prayer_allahumma_anta_translation,
        Res.string.athkar_post_prayer_ayat_kursi_transliteration     to Res.string.athkar_post_prayer_ayat_kursi_translation,
        Res.string.athkar_post_prayer_subhanallah_transliteration    to Res.string.athkar_post_prayer_subhanallah_translation,
        Res.string.athkar_post_prayer_alhamdulillah_transliteration  to Res.string.athkar_post_prayer_alhamdulillah_translation,
        Res.string.athkar_post_prayer_allahu_akbar_transliteration   to Res.string.athkar_post_prayer_allahu_akbar_translation,
        Res.string.athkar_post_prayer_la_ilaha_transliteration       to Res.string.athkar_post_prayer_la_ilaha_translation,
    )
) { item, (trans, transl) -> AthkarDisplayItem(item, trans, transl) }

/** Returns the display item list for the given [AthkarGroupType]. */
fun displayItemsFor(type: AthkarGroupType): List<AthkarDisplayItem> = when (type) {
    AthkarGroupType.MORNING     -> morningDisplayItems
    AthkarGroupType.EVENING     -> eveningDisplayItems
    AthkarGroupType.POST_PRAYER -> postPrayerDisplayItems
}
