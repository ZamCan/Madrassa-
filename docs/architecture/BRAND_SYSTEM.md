# EduNoor Brand & Design System — V7 "Emerald Night & Gold"

> **Purpose.** This document is the single reference for every future
> Madrassa — EduNoor screen. It records where the brand came from, the
> exact recipes for colors, glass surfaces, artifacts and icons, and
> the rules for applying them. Future engineering stages must preserve
> these definitions unless explicitly revised (mirrors
> `MASTER_PRODUCT_SPEC.md` §1).

---

## 1. Brand origins (approved references)

The visual identity is fused from three user-approved assets
(masters in `design/assets-src/`, all rights cleared by the product owner):

| Master file | Origin | Role |
|---|---|---|
| `icon_fullbleed.png` | "MY MADRASSA" wooden plaque emblem **fused with** the emerald frosted-glass tile + polished gold rim from the reference shot | App icon master (1024²) |
| `pattern_tile.png` | Milkish cream Islamic girih star-pattern (seamless tile) | Theme background for light surfaces |
| `artifacts_black.png` | Gold Islamic geometric line-art (octagram lattices, stepped "ladder" motifs, sparkles) on pure black | Decorative layer, screen-blended onto surfaces |

**Design language:** emerald night glass + gold rim + gold artifact
linework over a cream girih parchment. Dark-glass surfaces sit *on*
the cream canvas; the cream canvas is never fully replaced.

---

## 2. Regenerating brand assets — `design/pipeline.py`

All shipped bitmaps are **generated, never hand-edited**, from the
three masters:

```bash
pip install Pillow          # once
python3 design/pipeline.py  # from the repo root
```

Outputs (all under `app/src/main/res/`):

| Output | Purpose |
|---|---|
| `mipmap-*/ic_launcher.png`, `ic_launcher_round.png` | Legacy launcher icons (48–192 px) |
| `mipmap-*/ic_launcher_fg.png` | Adaptive-icon foreground layer (108–432 px); master is composed so the glass tile occupies the central ~66 % = the adaptive safe zone |
| `mipmap-xxxhdpi/ic_launcher_mono.png` | Monochrome layer for Android 13+ themed icons |
| `drawable-nodpi/brand_icon.png` | Rounded in-app brand tile (splash emblem, header mark, auth emblem) |
| `drawable-nodpi/edunoor_landing_bg.webp` | 1080×2400 cream girih canvas **with gold artifacts gilded on** (landing + auth screens) |
| `drawable-nodpi/edunoor_splash_bg.webp` | 1080×2280 emerald gradient (hex `#155941 → #04150F`) with artifacts screen-blended at 80 % |

**Why "screen blend":** `artifacts_black.png` is black with gold
lines. `ImageChops.screen(base, artifacts)` leaves black pixels
unchanged and adds the gold glow — the identical compositing trick the
approved reference uses. On the *light* landing canvas, pure screen
would wash out, so the pipeline first inks the linework toward bronze
`#926C32` (multiply/lerp) and then adds a soft gold glow pass. Keep
both passes if you regenerate.

When the brand is revised: replace a master file, run the pipeline,
commit. Never edit anything under `res/mipmap-*` or the two webp files
by hand.

---

## 3. Color tokens (`res/values/colors.xml`)

### Emerald night scale (dark surfaces)
| Token | Hex | Use |
|---|---|---|
| `edunoor_emerald` | `#061A13` | Base emerald night (window backgrounds on dark screens) |
| `edunoor_emerald_deep` | `#04120D` | Deepest tone — splash gradient end, adaptive-icon background, button label color |
| `edunoor_emerald_glass` | `#0C2B1F` | The glass tint — every frosted surface is this hue + alpha |
| `edunoor_emerald_primary` | `#155941` | Strong glass fill / brand accents; splash gradient start |

### Gold scale
| Token | Hex | Use |
|---|---|---|
| `edunoor_gold` | `#C69A45` | Rims, linework, ornaments (carried from V3) |
| `edunoor_gold_bright` | `#E8C97E` | Bright rims and hero accents on dark glass |
| `edunoor_gold_deep` | `#8A6A2C` | Gold button outer stroke |
| `edunoor_bronze` | `#926C32` | Artifact ink on light surfaces (pipeline only) |
| `edunoor_gold_soft` | `#E7D19B` | Subtitles on strong glass (carried from V3) |

### Light-surface + text tokens
| Token | Hex | Use |
|---|---|---|
| `edunoor_parchment` | `#F3E8D3` | Status/nav bar chrome on light screens |
| `edunoor_text_light` | `#F5EEDC` | Primary text on dark glass |
| `edunoor_text_faded` | `#C7B99B` | Secondary text, inactive utilities on dark glass |
| `ic_launcher_bg` | `#04120D` | Adaptive icon background color |

Legacy V3 tokens (walnut/clay/surface…) remain valid on parchment
surfaces (e.g. `edunoor_walnut #3E2920` text on cream).

**Rule:** dark surfaces always pair `text_light`/`text_faded` +
gold accents; light parchment surfaces pair walnut/clay text + gold
accents. Never place walnut text on glass or light text on parchment.

---

## 4. Glass recipes (drawables)

All glass = emerald `#0C2B1F` at varying alpha + a 1 dp gold hairline.
Corner radius scale: panels 24, cards 18, inputs/bar 13–14, buttons 26.

| Drawable | Fill | Stroke | Use |
|---|---|---|---|
| `glass_panel.xml` | `#B80C2B1F` (72 %) | 1 dp `#59C69A45` (35 %) | The large landing canvas hosting hero + cards |
| `glass_card_primary.xml` | `#D1155941` (82 % of *primary emerald*) | 1.5 dp solid gold | Primary role card (MZAZI) |
| `glass_card_secondary.xml` | `#D10C2B1F` (82 %) | 1 dp `#66C69A45` (40 %) | Secondary role card (USTADH) |
| `glass_input.xml` | `#A60C2B1F` (65 %) | 1 dp `#4DC69A45` (30 %) | Text inputs, segmented-control chips |
| `glass_bar.xml` | `#960C2B1F` (59 %) | 1 dp `#3DC69A45` (24 %) | Bottom utility bar |
| `gold_button.xml` | solid `edunoor_gold` | 1 dp `edunoor_gold_deep` | Primary actions (LOGIN / REGISTER) |
| `gold_ring_icon_frame.xml` | `#14000000` shadow disc | 1.5 dp gold oval ring | Behind `brand_icon` on splash + auth |
| `code_vertex.xml` | — | 1.2 dp gold L-bracket (10 dp vector) | Input-field corner artifacts |
| `ornament_divider.xml` | — | 1 dp gold | Line–diamond–line hero ornament |

**Component rules**
- Pressed feedback: follow the existing `ui/components` press behaviour.
- Buttons/labels on gold = `edunoor_emerald_deep` text, bold, ALL CAPS (see `gold_button.xml`).
- No drop shadows elsewhere — depth comes from glass alpha, gold rims, and the artifact layer.

**Spacing scale (programmatic UI).** `res/values/dimens.xml` was removed
in the V7 curation because the UI is built in code — the canonical
sizes live here (apply via the project's dp helpers):

| Element | dp |
|---|---|
| Page padding | 16 (landing) · 20 (auth) |
| Header height | 52 |
| Glass canvas padding | 17 sides · 20 top · 12 bottom |
| Role card height / radius | 66 / 18 |
| Input field height / radius | 50 / 14 |
| Utility bar height / radius | 48 / 13 |
| Gold button height / radius | 52 / 26 |
| Brand tile | 42 (header) · 96 (splash) · 76 (auth); ring = tile + 20/+16 |
| Hero block heights | bismillah 30 · title 56 · tagline 25 |

---

## 5. Artifacts — where the gold linework appears

The artifacts from the approved reference are applied at three scales:

1. **Surface level (baked into bitmaps).** `edunoor_landing_bg.webp`
   carries corner/edge artifact clusters gilded onto the pattern.
   Landing and Auth both use it as the root background, so role cards,
   the utility bar and the input boxes literally sit *on* the
   artifacts. `edunoor_splash_bg.webp` carries the glowing version on
   emerald.
2. **Component level (drawables).** `code_vertex.xml` gold L-brackets
   on input-field corners, `ornament_divider.xml` under the hero,
   `gold_ring_icon_frame.xml` around brand tiles.
3. **Type level.** Ornamental glyphs from the V6 system stay in use:
   `﷽` hero, `◈ / ◇` card marks, `✦` header, `۞` brand flanks,
   `— ◆ —` ornament fallback.

**Density rule:** artifacts decorate *gateways* (splash, landing,
auth). Future in-app **dashboards and dense UX should quiet down** —
use plain `edunoor_emerald`/parchment fills and reserve artifacts for
headers and empty-states, so data stays readable. (Spec: "Core
educational and management workflows should remain clean.")

---

## 6. Icon architecture

```
design/assets-src/icon_fullbleed.png   (1024² master)
        │ pipeline.py
        ├─ mipmap-anydpi-v26/ic_launcher.xml      adaptive icon
        │    ├─ background: @color/ic_launcher_bg
        │    ├─ foreground: @mipmap/ic_launcher_fg (glass tile in safe zone)
        │    └─ monochrome: @mipmap/ic_launcher_mono (themed icons)
        ├─ mipmap-{m,h,x,xx,xxx}dpi/ic_launcher_fg.png   foreground densities
        └─ drawable-nodpi/brand_icon.png          in-app rounded tile
```

minSdk is 26, so the adaptive XML resolves on every supported device —
no legacy bitmap launchers are shipped (the v0.7.1 hygiene pass removed
them). Keep it that way: `pipeline.py` no longer emits legacy bitmaps.
The `-v26` folder qualifier stays: AGP's AAPT2 does not resolve plain
`mipmap-anydpi` for adaptive icons (verified empirically), and the
single lint `ObsoleteSdkInt` note is accepted and documented.

The emblem (open Qur'an + crescent + globe/arrow + MY MADRASSA banner)
must never be redrawn by hand — always regenerate from the master.

---

## 7. Screen inventory (V7 stage)

| Screen | File | Background | Surfaces |
|---|---|---|---|
| Splash | `SplashActivity.java` | `edunoor_splash_bg` (emerald + artifacts) | Gold ring + brand tile, staggered fade-in |
| Landing | `MainActivity.java` | `edunoor_landing_bg` (cream pattern + artifacts) | `glass_panel` canvas, `glass_card_*` role cards, `glass_bar` utilities |
| Auth gateway | `AuthActivity.java` | `edunoor_landing_bg` | `glass_input` fields with `code_vertex` corners, `gold_button` submit, segmented glass chips |

Shared helpers live in `UiKit.java` (`dp`, `text`, `pressEffect`) —
never duplicate them per-Activity again.

**Layout rules:** programmatic UI (project convention), all sizes via
`UiKit.dp`, all user-visible text via `strings.xml` in the three
languages (Kiswahili default, English, Arabic). Arabic strings rely on
first-strong bidirectional detection — do **not** force
`Gravity.LEFT`/`TEXT_DIRECTION_RTL`; use `Gravity.START` only.

---

## 8. Don'ts

- Don't put gateway credentials, real OTP logic, or DB code into these screens (spec §15/§19 — later stages, server-side).
- Card glass sits at 82 % (`0xD1`): the accessibility pass proved 60–76 % composites
  fail WCAG AA for subtitle-size text on the cream canvas (gold_soft 3.25, text_faded
  2.45). Keep small text on glass to `text_light`/`text_faded`; the gold family stays
  on rims, symbols and arrows (large/decorative roles), never small subtitle text.
- The legacy V3 drawables (`parent_card`, `ustadh_card`, `edunoor_canvas`, …) are still used by existing `ui/components` screens — keep them working, but prefer the glass recipes for NEW premium surfaces.
- Don't stretch `brand_icon` beyond ~96 dp or the emblem detail turns muddy.
- Don't edit generated bitmaps; regenerate via the pipeline.

## 9. Release hygiene (v0.7.1 pass)

- **Backup/transfer policy:** `res/xml/data_extraction_rules.xml` (API 31+)
  and `res/xml/full_backup_content.xml` (API 26–30) exclude everything —
  recovery is owned by the app's encrypted-backup stage, never platform
  backup (spec §19). Manifest wires both plus `allowBackup="false"`.
- **Per-app languages:** `res/xml/locales_config.xml` (sw/en/ar) +
  `android:localeConfig` registers the LanguageManager switcher with the
  Android 13+ system language settings. `localeConfig` is intentionally
  API 33+ (lint `UnusedAttribute` note is by design).
- **API 31+ system splash:** `values-v31/styles.xml` tints the system
  splash with `edunoor_emerald_deep`, so system splash → SplashActivity
  reads as one entrance. (The custom in-app splash remains; migrating to
  androidx core-splashscreen is a future dependency decision.)
- **Accepted, documented warnings:** `UnusedResources` (design-system
  tokens + placeholders), `IconLauncherShape` on adaptive *foreground*
  layers (by-design full-bleed), `ObsoleteSdkInt` on `mipmap-anydpi-v26`
  (required, see §6), `AppBundleLocaleChanges` (APK distribution; AAB
  language splits are a future store decision), `DefaultLocale`,
  `ClickableViewAccessibility` (press effects route through
  `performClick()` where the view is the click target).

— Maintained by the EduNoor engineering stages. Last applied: v0.7.1 hygiene pass.
