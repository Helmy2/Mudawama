# Design System Document: The Serene Path

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

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section content. Boundaries must be defined solely through background color shifts. For example, a `surface-container-low` section sitting on a `surface` background creates a clear but "borderless" transition.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine, heavy-weight paper.
- **Layer 0 (Base):** `surface` (#fafaf5) – The world the app lives in.
- **Layer 1 (Sections):** `surface-container-low` (#f4f4ef) – Used for broad content groupings.
- **Layer 2 (Cards):** `surface-container-lowest` (#ffffff) – Use white cards on sand backgrounds to create a "lifted" feel without heavy shadows.

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

### Buttons (The "Thumb-First" Action)
- **Primary:** Gradient fill (`primary` to `primary-container`), `xl` roundedness, white text.
- **Secondary:** `surface-container-high` background with `primary` text. No border.
- **Tertiary:** Transparent background, `primary` text, underlined only on hover/active states.

### Habit Progress Rings
- **Visual Style:** Use a `12px` stroke width. The background of the ring should be `surface-container-highest`. The "filled" portion should use the `secondary` (Success Green) color.
- **Interaction:** On completion, trigger a haptic pulse and a soft glow expansion (4% opacity secondary color).

### Input Fields
- **Style:** Minimalist. No enclosing box. Use a `surface-container-low` background with a `3px` bottom-weighted indicator in `primary` only when focused.

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
