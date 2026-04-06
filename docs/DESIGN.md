# Design System Document: The Serene Path

---

## 0. Canonical UI References

All implementation MUST be faithful to the reference screens stored in `docs/ui/`. These images are the single source of truth for layout, copy, and component states. Any deviation requires an explicit design decision recorded in a PR description.

| File | Screen / Component |
|---|---|
| `welcome_to_mudawama.png` | Onboarding welcome — arch illustration, tagline, "Start My Journey" CTA |
| `home_dashboard.png` | Home — date strip, Next Prayer hero card, Daily Rituals grid, pending habits list |
| `daily_habits.png` | Daily Habits — Core Rituals section + Personal Habits section, Add New Habit button |
| `daily_prayer_tracker.png` | Today's Prayers — daily completion hero card, 5-prayer list with time + check toggle |
| `quran_daily_reading_tracker.png` | Quran Reading — progress ring, Goal card, Resume Reading card, Recent Logs list |
| `daily_athkar_tracker.png` | Daily Athkar — Morning / Evening / Post-Prayer Athkar cards, inspirational quote |
| `morning_athkar_reading.png` | Morning Athkar session — per-dhikr cards with Arabic text, TAP TO COUNT button |
| `post_prayer_athkar.png` | Post-Prayer Athkar — Tasbeeh Al-Fatima counters, Key Quranic Verses, Other Supplications |
| `tasbeeh_counter.png` | Tasbeeh Counter — large count ring, TAP SCREEN CTA, Reset/Goal actions, session stats |
| `insights_progress.png` | Insights — weekly heatmap, streak card, Prayer Completion stats, Quran Reading stats |
| `settings.png` | Settings — Preferences, Analytics, Daily Intentions, Data, About sections |
| `new_habit_bottom_sheet.png` | New Habit sheet — name field, icon picker, frequency day selector, goal type selector |
| `manage_habit_bottom_sheet.png` | Manage Habit sheet — Edit / Reset Today's Progress / Delete actions |
| `log_reading_bottom_sheet.png` | Log Reading sheet — page count ring, quick-add chips, current position row |
| `quran_reading_goal_bottom_sheet.png` | Daily Quran Goal sheet — stepper, popular goals chips |
| `select_surah_ayah_bottom_sheet.png` | Update Position sheet — searchable surah list + ayah number picker |
| `tasbeeh_goal_bottom_sheet.png` | Daily Tasbeeh Goal sheet — target count stepper, preset tiles (33/100/300) |

---

## 1. String Resources Rule (NON-NEGOTIABLE)

Every user-visible string displayed in a `@Composable` function MUST be
declared in `shared/designsystem/src/commonMain/composeResources/values/strings.xml`
and accessed via `stringResource(Res.string.<key>)`.

- **Hardcoded string literals inside `@Composable` functions are forbidden**
  (e.g., `Text("Next Prayer")` is a violation; `Text(stringResource(Res.string.home_next_prayer_label))` is correct).
- Key naming convention: `<screen>_<element>_<type>` in `snake_case`.
  Examples: `home_next_prayer_label`, `habits_add_new_habit_button`,
  `quran_log_reading_title`, `athkar_morning_start_button`.
- Format/placeholder strings use `%1$s` / `%1$d` positional args.
- Static labels visible in the UI reference images (section headings,
  button labels, placeholder text, motivational copy) MUST each have a
  dedicated string resource key — they must not be embedded in code.

**Single source of truth.** There is exactly one `strings.xml` in the
project. Feature modules MUST NOT create their own `strings.xml` or
`composeResources/` directory.

**Correct `Res` import.** The `mudawama.kmp.compose` convention plugin
derives the generated `Res` class package from the Gradle module path
(`"mudawama." + gradlePath.trimStart(':').replace(':', '.')`). For
`shared/designsystem` this is `mudawama.shared.designsystem`. Every
file that needs a string resource must import:

```kotlin
import mudawama.shared.designsystem.Res
```

The package `io.github.helmy2.mudawama.designsystem.generated.resources`
does **not** exist and will not compile. A `Res as DsRes` alias is
forbidden — there is only one `Res` in the codebase.

This rule exists to support full Arabic (`ar`) localization, which is a
core product value, without requiring any Kotlin source changes.

---

## 1. Overview & Creative North Star
**Creative North Star: "The Digital Sanctuary"**

This design system moves beyond the utility of a standard tracker to create a high-end, editorial experience. It is rooted in the concept of *Muraqaba* (mindful awareness), translated into a UI that feels like a quiet, sunlit room. We avoid "standard" mobile app clutter by embracing intentional asymmetry, expansive breathing room, and a rejection of traditional structural lines.

The aesthetic is **Soft Minimalism**. We do not use geometric patterns or literal ornaments to signal identity; instead, we use a sophisticated typographic scale and a "liquid" layout where elements feel floated rather than boxed. Every interaction must feel intentional, hushed, and premium.

---

## 2. Colors & Atmospheric Depth

The palette is anchored in nature: the deep permanence of the emerald forest and the shifting softness of desert sands.

### Core Palette (Material 3 Derived)
- **Primary:** `#004f45` (The Anchor)
- **Primary Container:** `#00695c` (The Brand Presence)
- **Surface / Background:** `#fafaf5` (The Warm Canvas)
- **Secondary (Success):** `#006e1c` (The 'Completed' Growth)
- **Tertiary (Pending):** `#444646` (The Quiet Neutral)

### Implementation Tokens (`MudawamaColors`)

The design system exposes a `MudawamaColors` data class with the following semantic slots, provided via `MudawamaTheme.colors.*`:

| Token | Light value | Dark value | Usage |
|---|---|---|---|
| `primary` | `#02594F` DeepTeal | `#4DB882` EmeraldLight | CTAs, progress rings, active icons |
| `onPrimary` | `#FFFFFF` | `#003828` | Text/icons on primary-colored surfaces |
| `background` | `#F7F7F4` OffWhite | `#0F1412` DarkBackground | Page-level fill (deepest layer) |
| `surface` | `#FFFFFF` | `#1A2120` DarkSurface | Cards and sheets — one step above background |
| `surfaceVariant` | `#F7F7F4` OffWhite | `#243130` DarkSurfaceVariant | Icon chips, secondary containers — two steps above background |
| `onSurface` | `#1D2322` Charcoal | `#E8F0EF` DarkOnSurface | Body text, secondary icons |
| `error` | `#C45151` MutedRed | `#FF6B6B` DarkError | Destructive actions, validation errors |

**Dark mode rationale:**
- `EmeraldLight (#4DB882)` replaces `CalmEmerald (#1B8049)` as primary — the old value was too dark to achieve AA contrast on near-black backgrounds.
- Background (`#0F1412`) and surface (`#1A2120`) are clearly separated (≈5% luminance step) so cards are visible without borders.
- `surfaceVariant (#243130)` gives icon chips a distinct background — without this slot they blended into the card on dark surfaces.
- `DarkOnSurface (#E8F0EF)` is a warm off-white rather than pure white, reducing eye strain in dark environments.
- `DarkError (#FF6B6B)` is brighter than the light-mode `#C45151` so error text remains legible on dark backgrounds.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section content. Boundaries must be defined solely through background color shifts. For example, a `surface-container-low` section sitting on a `surface` background creates a clear but "borderless" transition.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine, heavy-weight paper.
- **Layer 0 (Base):** `background` — The world the app lives in.
- **Layer 1 (Cards / Sheets):** `surface` — Use for habit cards, bottom sheets, dialog backgrounds.
- **Layer 2 (Chips / Secondary containers):** `surfaceVariant` — Use for icon chips and any element that needs to stand out from a `surface` card without using `primary`.

### Signature Textures (Glass & Gradient)
To achieve a premium editorial feel:
- **The Prayer/Habit Glow:** Use a subtle linear gradient transitioning from `primary` (#004f45) to `primary_container` (#00695c) at a 135° angle for hero CTA buttons and active progress rings. This provides a "soul" that flat colors lack.
- **Floating Navigation:** Use Glassmorphism for the bottom navigation bar. Use `surface_container_lowest` at 80% opacity with a `20px` backdrop-blur. This ensures the app feels deep and integrated.

---

## 3. Typography: The Editorial Voice

We use a dual-font system to create high-contrast hierarchy.
- **Display & Headlines (Manrope):** This is our "Authoritative" voice. Use `display-lg` (3.5rem) for daily streaks or mosque names. The wide apertures of Manrope feel modern and welcoming.
- **Body & Labels (Inter):** Our "Functional" voice. Inter is used for habit descriptions and settings for maximum legibility at small scales.

**Hierarchy Tip:** Never use "Bold" for body text. Use "Medium" (500) for emphasis and "Regular" (400) for standard reading. Premium design relies on size contrast rather than weight contrast.

---

## 4. Elevation & Depth

We convey importance through **Tonal Layering**, not structural lines.

### The Layering Principle
Depth is achieved by "stacking" the surface-container tiers. Place a `surface-container-lowest` (#ffffff) card on a `surface-container-low` (#f4f4ef) section. This creates a soft, natural lift.

### Ambient Shadows
If a floating effect is required (e.g., a "Complete Habit" FAB):
- **Color:** Use a tinted version of `on-surface` (e.g., `#1a1c19` at 6% opacity).
- **Blur:** Extra-diffused. Use a `Y: 8px, Blur: 24px` setting. Avoid "dirty" grey shadows.

### The "Ghost Border" Fallback
If a border is required for accessibility (e.g., an unselected habit state), use the `outline-variant` token at **15% opacity**. Total opacity borders are strictly forbidden.

---

## 5. Components

### Cards & Habit Lists
- **No Dividers:** Forbid the use of divider lines. Separate list items using `spacing-4` (1.4rem) or subtle background shifts between cards.
- **Roundedness:** Use `xl` (1.5rem / 24px) for habit cards to create a soft, organic feel. Use `full` (9999px) for progress bars and chips.
- **Core Rituals row** (`daily_habits.png`): circular progress ring (green stroke) + title + subtitle. Uses `surface` card on `background`. The ring shows `N/goal` for NUMERIC habits or a full green ring + check for completed BOOLEAN habits.
- **Personal Habits row — BOOLEAN** (`daily_habits.png`): `surfaceVariant` icon chip (teal icon tint) + habit name + `RadioButtonUnchecked` / `CheckCircle` toggle + three-dot overflow menu. The entire card is tappable and toggles completion. Completed state: name dimmed to 50% alpha, icon becomes filled `CheckCircle` in `primary` teal.
- **Personal Habits row — NUMERIC (counter)**: same icon chip layout, but the trailing control is a `− count/goal +` stepper using `FilledIconButton` with `primary.copy(alpha=0.12)` container. The card itself is not tappable; only the `−`/`+` buttons trigger count changes. Count label turns `primary` teal when the habit is completed.
- **Prayer list row** (`daily_prayer_tracker.png`): circular icon (color-coded per prayer) + name + time + circular checkbox toggle.
- **Habit list row** (`home_dashboard.png`): icon + name + category label + circular checkbox. Pending/Completed segmented control above list.

### Hero Cards
- **Next Prayer card** (`home_dashboard.png`): full-width, deep teal (`#004f45`) fill, rounded `xl`. Displays label "NEXT PRAYER", prayer name (Display-LG), countdown "X:XX minutes remaining", and a green linear progress bar at bottom.
- **Daily Completion card** (`daily_prayer_tracker.png`): deep teal fill, "DAILY COMPLETION", fraction display (e.g., "2 / 5"), circular progress ring at right.
- **Streak card** (`insights_progress.png`): deep teal fill, flame emoji, "X Day Streak", motivational subtitle.

### Progress Rings
- **Visual Style:** `12px` stroke width. Background track: `surface-container-highest`. Filled arc: `secondary` (Success Green `#006e1c`). Completed state: full ring + checkmark icon.
- **Sizes:** Large (Quran/Tasbeeh screens, ~160dp), Medium (Daily Rituals grid cards, ~56dp), Small (habit list rows, ~40dp).
- **Interaction:** On completion, trigger haptic pulse and soft glow expansion (4% opacity secondary color).

### Bottom Sheets
All bottom sheets follow a consistent structure (see reference images):
- Drag handle pill at top center.
- Header row: close `×` icon (left), title (center), primary action button or text link (right).
- Content body with appropriate input controls.
- Sheets: New Habit, Manage Habit, Log Reading, Daily Quran Goal, Update Position, Daily Tasbeeh Goal.

### Buttons (The "Thumb-First" Action)
- **Primary:** Deep teal fill (`#004f45`), `xl` roundedness, white text. Used for main CTAs ("Start My Journey", "Log Reading", "TAP SCREEN", "Save").
- **Secondary:** `surface-container-high` background with `primary` text. No border. Used for quick-add chips ("+1 Page", "+5 Pages", "1 Juz").
- **Tertiary:** Transparent background, `primary` text, underlined only on hover/active states.
- **Destructive:** Red fill / red text for delete actions (see `manage_habit_bottom_sheet.png`).

### Date Strip
(`home_dashboard.png`, `daily_prayer_tracker.png`): horizontal row of day/date pill chips. Active day: deep teal fill + white text + dot indicator below. Inactive: `surface-container` fill + `on-surface-variant` text.

### Bottom Navigation Bar
4 tabs: **Home**, **Prayers**, **Quran**, **Athkar**. Active tab: rounded square deep teal container with white icon + label. Inactive: icon + label in `on-surface-variant`. Floating glassmorphism style (80% opacity, 20dp blur, 28dp corner radius, 16dp horizontal margin).

### Input Fields
- **Style:** Minimalist. `surface-container-low` background, `full` roundedness, no visible border box. Placeholder text in `on-surface-variant`. Used in New Habit name field and Search Surah field.

### Tasbeeh Counter
(`tasbeeh_counter.png`): Large neumorphic circle with count + dhikr label. Outer arc progress ring. Three action controls below: Reset (icon button), TAP SCREEN (primary wide button), Goal (icon button). Stats row at bottom showing "TODAY'S TOTAL" and "CURRENT SESSION".

---

## 6. Do’s and Don'ts

### Do
- **DO** use whitespace as a functional element. If a screen feels "empty," it is likely working.
- **DO** align text to the left for headlines to maintain an editorial, high-end magazine feel.
- **DO** use "One-Thumb" ergonomics. Place all primary interactions (Check habit, Add habit) within the bottom 40% of the screen.

### Don't
- **DON'T** use "Islamic" clip-art or complex star patterns. The "Muslim" identity is conveyed through the serenity of the palette and the respect for time/ritual.
- **DON'T** use pure black (#000000). Use `on-surface` (#1a1c19) for high-contrast text.
- **DON'T** use 8-bit or "playful" animations. Transitions should be "Linear-to-Ease-Out" and last no longer than 300ms.

---

## 7. Spacing Scale (8pt Derivative)

Our spacing is intentional to prevent "crowding."
- **Standard Padding:** `spacing-4` (1.4rem) for internal card padding.
- **Section Gaps:** `spacing-8` (2.75rem) between major habit categories (e.g., Salah vs. Personal Habits).
- **Touch Targets:** Minimum `spacing-12` (4rem) for any interactive element.
