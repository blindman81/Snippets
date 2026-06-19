# Design System Strategy: The Organic Curator

## 1. Overview & Creative North Star
The "Organic Curator" is the creative North Star for this design system. We are moving away from the clinical, data-heavy feel of traditional calorie trackers toward a high-end editorial experience that celebrates food as art. 

This system rejects the "standard app" aesthetic by embracing **Organic Brutalism**—a style that combines the bold, structural confidence of Material 3 with the soft, breathing room of a premium lifestyle magazine. We achieve this through:
*   **Intentional Asymmetry:** Breaking the grid with oversized food photography that bleeds off-edge.
*   **Tonal Depth:** Replacing harsh lines with sophisticated surface nesting.
*   **The "No-Nav" Freedom:** Since we’ve eliminated the bottom navigation bar, we utilize a "Floating Command" architecture where primary actions live in a dynamic, high-radius header or a contextual floating element, maximizing screen real estate for immersive imagery.

---

## 2. Colors & Surface Philosophy
Our palette is a dialogue between "Harvest Greens" and "Cereal Ambers," rooted in a clean, neutral foundation.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section content. Boundaries must be defined solely through background color shifts or subtle tonal transitions.
*   **Primary Sectioning:** Use `surface` as the base.
*   **Content Grouping:** Use `surface-container-low` for large content blocks.
*   **Nested Elements:** Place a `surface-container-lowest` card on top of a `surface-container-low` background to create "natural" separation.

### The "Glass & Gradient" Rule
To elevate the "M3E" experience beyond a generic utility:
*   **Signature Textures:** Apply a linear gradient from `primary` (#0d631b) to `primary-container` (#2e7d32) on hero CTAs (e.g., "Log Meal"). This adds "soul" and depth.
*   **Glassmorphism:** For top app bars or floating snack-bars, use `surface_container_lowest` at 80% opacity with a `24px` backdrop blur. This allows the vibrant food photography to "glow" through the interface.

---

## 3. Typography: The Editorial Voice
We utilize a pairing of **Plus Jakarta Sans** for character and **Manrope** for high-performance readability.

*   **Display (Plus Jakarta Sans):** Used for daily summaries and macronutrient "hero" numbers. Use `display-lg` (3.5rem) to make health data feel like a magazine headline.
*   **Headline (Plus Jakarta Sans):** Used for recipe names and section headers. These should be bold and authoritative.
*   **Body (Manrope):** The workhorse for ingredients and logging descriptions. The `body-lg` (1rem) provides ample tracking and line height (1.5x) for effortless scanning.
*   **Label (Manrope):** Used for metadata (e.g., "15 mins" or "400 kcal"). Use `label-md` in uppercase with a `0.05rem` letter spacing to create a professional, "spec-sheet" aesthetic.

---

## 4. Elevation & Depth: The Layering Principle
We do not use shadows to represent "height"; we use color density to represent "importance."

*   **Tonal Layering:**
    *   **Level 0 (Base):** `surface` (#f9f9f9).
    *   **Level 1 (Sections):** `surface-container-low` (#f3f3f3).
    *   **Level 2 (Cards):** `surface-container-lowest` (#ffffff). This creates a crisp, "lifted" effect without a single drop shadow.
*   **Ambient Shadows:** If an element must float (like a Floating Action Button), use a shadow tinted with the `primary` color (e.g., `rgba(13, 99, 27, 0.08)`) with a `32px` blur. Never use pure black or grey shadows.
*   **The "Ghost Border":** If accessibility requires a container edge (e.g., in a search bar), use `outline-variant` (#bfcaba) at **15% opacity**. It should be felt, not seen.

---

## 5. Components

### High-Impact Food Cards
*   **Architecture:** Use `radius-lg` (2rem) or `radius-xl` (3rem). 
*   **Imagery:** Images must use a 4:5 aspect ratio. Top corners should be rounded to the card's radius, but the bottom of the image should bleed into a `surface-container-lowest` area where typography lives.
*   **Rule:** No dividers. Separate the "Description" from the "Calories" using vertical white space (32px).

### Buttons & Inputs
*   **Primary Button:** `radius-full` (9999px). Use the `primary` gradient mentioned in Section 2. Text should be `title-sm` in `on_primary`.
*   **Chips:** Use `secondary_container` for active filters. The high-amber (#feb300) acts as a "visual vitamin," drawing the eye to active selections.
*   **Input Fields:** Ghost-style inputs only. No background fill; only a subtle `surface-variant` underline or a very soft `surface-container-high` background with `radius-md`.

### The "Pulse" Progress Bar
*   Instead of a standard thin line, use a thick (`12px`) track with `radius-full`.
*   The track should be `surface-container-highest`, and the fill should be a gradient from `primary` to `inverse_primary`.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** allow food photos to take up at least 40% of the screen real estate.
*   **Do** use `radius-xl` (3rem) for main dashboard containers to create a soft, friendly "hug" for the content.
*   **Do** use `tertiary` (Warm Amber) for "Warning" or "Goal Near" states instead of `error` red, to keep the vibe appetizing and encouraging.

### Don't:
*   **Don't** use 1px dividers. If you need to separate items in a list, use a 16px vertical gap or a subtle shift from `surface` to `surface-container-low`.
*   **Don't** use pure black (#000000) for text. Always use `on_surface` (#1a1c1c) to maintain a soft, premium feel.
*   **Don't** use small corner radii. If it’s not rounded by at least `1rem`, it’s too sharp for this system’s personality.