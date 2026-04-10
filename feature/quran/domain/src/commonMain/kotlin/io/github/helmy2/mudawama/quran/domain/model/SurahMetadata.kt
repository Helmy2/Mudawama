package io.github.helmy2.mudawama.quran.domain.model

/**
 * Metadata for a single Surah of the Quran.
 *
 * @param number    Surah number, 1–114.
 * @param nameEn    Transliterated English name.
 * @param ayahCount Total number of ayahs in this surah.
 * @param startPage The page number (1–604) in the Madinah Mushaf (Uthmani script)
 *                  on which this surah begins. Used to advance the reading bookmark
 *                  automatically when the user logs pages read.
 */
data class SurahMetadata(
    val number: Int,
    val nameEn: String,
    val ayahCount: Int,
    val startPage: Int,
)

/**
 * All 114 surahs with their Madinah Mushaf start pages.
 *
 * Start-page values follow the standard 604-page Madinah Mushaf
 * (King Fahd Complex print), which is the most widely distributed
 * Quran edition and the basis for most digital Quran apps.
 */
val ALL_SURAHS: List<SurahMetadata> = listOf(
    SurahMetadata(1,   "Al-Fatihah",       7,   1),
    SurahMetadata(2,   "Al-Baqarah",     286,   2),
    SurahMetadata(3,   "Aal-E-Imran",    200,  50),
    SurahMetadata(4,   "An-Nisa",        176,  77),
    SurahMetadata(5,   "Al-Maidah",      120, 106),
    SurahMetadata(6,   "Al-Anam",        165, 128),
    SurahMetadata(7,   "Al-Araf",        206, 151),
    SurahMetadata(8,   "Al-Anfal",        75, 177),
    SurahMetadata(9,   "At-Tawbah",      129, 187),
    SurahMetadata(10,  "Yunus",           109, 208),
    SurahMetadata(11,  "Hud",             123, 221),
    SurahMetadata(12,  "Yusuf",           111, 235),
    SurahMetadata(13,  "Ar-Rad",           43, 249),
    SurahMetadata(14,  "Ibrahim",          52, 255),
    SurahMetadata(15,  "Al-Hijr",          99, 262),
    SurahMetadata(16,  "An-Nahl",         128, 267),
    SurahMetadata(17,  "Al-Isra",         111, 282),
    SurahMetadata(18,  "Al-Kahf",         110, 293),
    SurahMetadata(19,  "Maryam",           98, 305),
    SurahMetadata(20,  "Ta-Ha",           135, 312),
    SurahMetadata(21,  "Al-Anbiya",       112, 322),
    SurahMetadata(22,  "Al-Hajj",          78, 332),
    SurahMetadata(23,  "Al-Muminun",      118, 342),
    SurahMetadata(24,  "An-Nur",           64, 350),
    SurahMetadata(25,  "Al-Furqan",        77, 359),
    SurahMetadata(26,  "Ash-Shuara",      227, 367),
    SurahMetadata(27,  "An-Naml",          93, 377),
    SurahMetadata(28,  "Al-Qasas",         88, 385),
    SurahMetadata(29,  "Al-Ankabut",       69, 396),
    SurahMetadata(30,  "Ar-Rum",           60, 404),
    SurahMetadata(31,  "Luqman",           34, 411),
    SurahMetadata(32,  "As-Sajdah",        30, 415),
    SurahMetadata(33,  "Al-Ahzab",         73, 418),
    SurahMetadata(34,  "Saba",             54, 428),
    SurahMetadata(35,  "Fatir",            45, 434),
    SurahMetadata(36,  "Ya-Sin",           83, 440),
    SurahMetadata(37,  "As-Saffat",       182, 446),
    SurahMetadata(38,  "Sad",              88, 453),
    SurahMetadata(39,  "Az-Zumar",         75, 458),
    SurahMetadata(40,  "Ghafir",           85, 467),
    SurahMetadata(41,  "Fussilat",         54, 477),
    SurahMetadata(42,  "Ash-Shura",        53, 483),
    SurahMetadata(43,  "Az-Zukhruf",       89, 489),
    SurahMetadata(44,  "Ad-Dukhan",        59, 496),
    SurahMetadata(45,  "Al-Jathiyah",      37, 499),
    SurahMetadata(46,  "Al-Ahqaf",         35, 502),
    SurahMetadata(47,  "Muhammad",         38, 507),
    SurahMetadata(48,  "Al-Fath",          29, 511),
    SurahMetadata(49,  "Al-Hujurat",       18, 515),
    SurahMetadata(50,  "Qaf",              45, 518),
    SurahMetadata(51,  "Adh-Dhariyat",     60, 520),
    SurahMetadata(52,  "At-Tur",           49, 523),
    SurahMetadata(53,  "An-Najm",          62, 526),
    SurahMetadata(54,  "Al-Qamar",         55, 528),
    SurahMetadata(55,  "Ar-Rahman",        78, 531),
    SurahMetadata(56,  "Al-Waqiah",        96, 534),
    SurahMetadata(57,  "Al-Hadid",         29, 537),
    SurahMetadata(58,  "Al-Mujadila",      22, 542),
    SurahMetadata(59,  "Al-Hashr",         24, 545),
    SurahMetadata(60,  "Al-Mumtahanah",    13, 549),
    SurahMetadata(61,  "As-Saf",           14, 551),
    SurahMetadata(62,  "Al-Jumuah",        11, 553),
    SurahMetadata(63,  "Al-Munafiqun",     11, 554),
    SurahMetadata(64,  "At-Taghabun",      18, 556),
    SurahMetadata(65,  "At-Talaq",         12, 558),
    SurahMetadata(66,  "At-Tahrim",        12, 560),
    SurahMetadata(67,  "Al-Mulk",          30, 562),
    SurahMetadata(68,  "Al-Qalam",         52, 564),
    SurahMetadata(69,  "Al-Haqqah",        52, 566),
    SurahMetadata(70,  "Al-Maarij",        44, 568),
    SurahMetadata(71,  "Nuh",              28, 570),
    SurahMetadata(72,  "Al-Jinn",          28, 572),
    SurahMetadata(73,  "Al-Muzzammil",     20, 574),
    SurahMetadata(74,  "Al-Muddaththir",   56, 575),
    SurahMetadata(75,  "Al-Qiyamah",       40, 577),
    SurahMetadata(76,  "Al-Insan",         31, 578),
    SurahMetadata(77,  "Al-Mursalat",      50, 580),
    SurahMetadata(78,  "An-Naba",          40, 582),
    SurahMetadata(79,  "An-Naziat",        46, 583),
    SurahMetadata(80,  "Abasa",            42, 585),
    SurahMetadata(81,  "At-Takwir",        29, 586),
    SurahMetadata(82,  "Al-Infitar",       19, 587),
    SurahMetadata(83,  "Al-Mutaffifin",    36, 587),
    SurahMetadata(84,  "Al-Inshiqaq",      25, 589),
    SurahMetadata(85,  "Al-Buruj",         22, 590),
    SurahMetadata(86,  "At-Tariq",         17, 591),
    SurahMetadata(87,  "Al-Ala",           19, 591),
    SurahMetadata(88,  "Al-Ghashiyah",     26, 592),
    SurahMetadata(89,  "Al-Fajr",          30, 593),
    SurahMetadata(90,  "Al-Balad",         20, 594),
    SurahMetadata(91,  "Ash-Shams",        15, 595),
    SurahMetadata(92,  "Al-Layl",          21, 595),
    SurahMetadata(93,  "Ad-Duha",          11, 596),
    SurahMetadata(94,  "Ash-Sharh",         8, 596),
    SurahMetadata(95,  "At-Tin",            8, 597),
    SurahMetadata(96,  "Al-Alaq",          19, 597),
    SurahMetadata(97,  "Al-Qadr",           5, 598),
    SurahMetadata(98,  "Al-Bayyinah",       8, 598),
    SurahMetadata(99,  "Az-Zalzalah",       8, 599),
    SurahMetadata(100, "Al-Adiyat",        11, 599),
    SurahMetadata(101, "Al-Qariah",        11, 600),
    SurahMetadata(102, "At-Takathur",       8, 600),
    SurahMetadata(103, "Al-Asr",            3, 601),
    SurahMetadata(104, "Al-Humazah",        9, 601),
    SurahMetadata(105, "Al-Fil",            5, 601),
    SurahMetadata(106, "Quraysh",           4, 602),
    SurahMetadata(107, "Al-Maun",           7, 602),
    SurahMetadata(108, "Al-Kawthar",        3, 602),
    SurahMetadata(109, "Al-Kafirun",        6, 603),
    SurahMetadata(110, "An-Nasr",           3, 603),
    SurahMetadata(111, "Al-Masad",          5, 603),
    SurahMetadata(112, "Al-Ikhlas",         4, 604),
    SurahMetadata(113, "Al-Falaq",          5, 604),
    SurahMetadata(114, "An-Nas",            6, 604),
)

/**
 * Returns the surah that contains the given [page] (1–604).
 * Always returns a valid surah — falls back to the last surah if page > 604.
 */
fun surahForPage(page: Int): SurahMetadata {
    val clamped = page.coerceIn(1, 604)
    return ALL_SURAHS.lastOrNull { it.startPage <= clamped } ?: ALL_SURAHS.first()
}
