# Madrassa — EduNoor UI Engineering Report (Boxes 1–3)

> **Historical report.** The claims below describe the earlier
> `32c12f7`-era batch. The current `final-verification` branch is based on
> the actual `origin/main` tree and adds tenant-scoped dashboards, Islamic
> tools, security checks, and a fresh debug build. Device installation and
> logcat verification remain blocked until a physical phone is attached.

Date: 2026-09-23
Project: `/data/data/com.termux/files/home/Madrassa`
Scope: Box 1 (Foundation & Responsive UI), Box 2 (Forms, Registration &
Languages), Box 3 (Integration, Branding & Verification).
Governing documents: `docs/architecture/MASTER_PRODUCT_SPEC.md`,
`docs/BUILD_REFERENCE.md`.

---

## 1. Executive summary

All three boxes are complete and the app builds on the canonical manual
toolchain.

| Area | Result |
|------|--------|
| Design system | One palette (`res/values/colors.xml`) drives every component, activity and window bar. **0** `Color.rgb(...)` literals remain in live sources (was 28). |
| Responsive layout | Every fixed-height text/card row that could clip (including the 97dp-content-in-82dp card bug) is now `WRAP_CONTENT` + margins — heights behave as minimums. |
| Forms | Persistent floating labels, non-blocking password toggle, number/email/phone/password modes, IME action chaining, clear-error-on-type, keyboard dismissed before pickers open. |
| Languages | Swahili/English/Arabic, **170 / 170 / 170** identical key sets; `attachBaseContext(LanguageManager.wrap)` on every activity; true RTL honoured. |
| Startup | Splash no longer sleeps a fixed 1100 ms — it performs real initialization (open store → load records → check status) with a live status line; post-login uses the new `InitializationActivity` (spec §11). |
| Registration | Submits through the domain `RegistrationService` (policy → PBKDF2 hash → single SQLite transaction), off the main thread, with per-error-code localized messages. |
| Verification | Pre-build checklist §5 passed; **both builds green** — manual `BUILD COMPLETE` (334K APK, v2+v3) and Gradle `BUILD SUCCESSFUL in 2m 43s`; string parity, manifest and zero-byte checks all green. All work **committed and pushed** to `origin/main`. |

---

## 2. Box 1 — Foundation & responsive UI

### 2.1 Single EduNoor design system

All colors resolve from `R.color.edunoor_*` in `res/values/colors.xml`:

| Token | Value | Role |
|-------|-------|------|
| `edunoor_walnut` / `edunoor_ink` | `#3E2920` | Primary text, dark bars |
| `edunoor_clay` | `#A95F3D` | Primary action, accents |
| `edunoor_clay_dark` / `edunoor_clay_soft` | `#81452F` / `#E8C9B5` | Gradient stop / secondary fill |
| `edunoor_surface` | `#FFF9ED` | Parchment background (ivory) |
| `edunoor_muted` | `#806E60` | Secondary text |
| `edunoor_gold` / `edunoor_gold_soft` | `#C69A45` / `#E7D19B` | Brand Arabic line, splash |
| `edunoor_border` / `edunoor_dusty_blue` | `#D8C5A8` / `#5E7180` | Field border / focus |

Components no longer carry their own literals: `FloatingOutlineField`,
`EduNoorCard`, `EduNoorButton`, `EduNoorStateView`, `EduNoorProgressView`
and every touched activity resolve palette entries at construction.

### 2.2 Component fixes

- **`EduNoorCard.create`** — content rows were fixed at 18/25/32dp inside an
  82dp card while real content measured 97dp, so text clipped. Rows are now
  `WRAP_CONTENT` with margins; the card grows with its content.
- **`EduNoorStateView`** — `minHeight 130dp` plus wrapping rows (loading /
  empty / error copy no longer truncates in any of the three languages).
- **`EduNoorProgressView`** — wrapping heading and a `55dp` minimum counter
  width so `12/40`-style counters never collide with the label.
- **`EduNoorButton`** — now carries a disabled-state guard in its touch
  listener: while an async login/submission has locked the button, a press is
  swallowed entirely (no press animation, no click).

### 2.3 Responsive layout sweep

Fixed heights were converted to `WRAP_CONTENT` + margins everywhere text is
localizable or dynamic:

- **Landing (`MainActivity`)** — header, brand, bismillah, hero title,
  tagline, bottom bar.
- **Solo Learning (`SoloLearningActivity`)** — intro, cards, section titles,
  progress section (plus an 18dp top margin), state view.
- **Registration (`MadrassaRegistrationActivity`)** — brand, Arabic line,
  step label, title, subtitle, every form field, password-rules line, review
  sections/rows, submitted panel, and the Back/Next navigation row.
- **Auth screens** — wrapping rows throughout (Box 2).

Because a `WRAP_CONTENT` height acts as a minimum, Arabic (RTL, often longer)
and Swahili translations now expand instead of clipping.

### 2.4 Type & spacing tokens

`res/values/dimens.xml` gained the Box 1 token set alongside the existing
Visual V4 values: `space_xxs…space_xl` (2–24dp), `control_height` (48dp),
`field_height` (58dp), and `text_caption/body/subtitle/title`
(12/14/16/23sp).

---

## 3. Box 2 — Forms, registration & languages

### 3.1 `FloatingOutlineField` overhaul

- **Password visibility toggle** — previously a full-width clickable layer
  that swallowed every tap on the password input. It is now confined to a
  right-edge **46dp strip** (`FrameLayout END` gravity), with localized
  content descriptions (`show_password` / `hide_password`).
- **Persistent floating label** — the label is always visible and the hint
  carries an example value, so an empty field never hides its own name.
- **Clear-error-on-type** — a `TextWatcher` drops the red error border as
  soon as the user corrects the value.
- **Keyboard behaviour** — default `IME_ACTION_NEXT`; new
  `setNumberMode()`, `setEmailMode()`, `setOnDoneAction()` let each field
  declare its input type and end-of-form action.
- **Modes** — text / phone (with tappable country prefix) / password /
  picker (chevron) / typing fallback.

### 3.2 Authentication screens

`ParentLoginActivity` and `UstadhLoginActivity` were rewritten on the design
system:

- `EduNoorButton.primary` / `.secondary` instead of platform-tinted native
  `Button`s.
- Validation via `EduNoorRules` (`validPhone`, `normalizePhone`,
  `validEmail`) with messages that name the failing field.
- **Async authentication** on a single-thread `ExecutorService` — PBKDF2 runs
  at 210,000 iterations and must never block the main thread; a busy guard
  prevents double submission and `onDestroy()` shuts the worker down.
- Every `AuthenticationResult.Status` case is handled, including
  `MADRASSA_INACTIVE` → `InitializationActivity.forUstadh(...)` and
  `PASSWORD_CHANGE_REQUIRED`.
- **Phone contract** — the UI normalizes national formats
  (`0712 345 678`) to `+255 712345678` before calling services, while the
  Ustadh identifier passes `+`/`00` numbers through untouched
  (`normaliseIdentifier`), so a foreign `+254` number is never rewritten as
  `+255`.

### 3.3 Registration form

- Colors → palette; fixed heights → `WRAP_CONTENT` (header, fields, review,
  submitted, navigation).
- Back/Next are `EduNoorButton`s; `ustadhCount` uses number mode, `email`
  uses email mode.
- The soft keyboard is dismissed (`hideKeyboard()`) before the region,
  district and country-code pickers open, so the dialog is never covered.
- Existing strengths kept: region→district cascade over the offline Tanzania
  dataset, draft restore across Back/Next, the `—` review placeholder never
  being captured, country-neutral phone messaging.

### 3.4 Languages

- `attachBaseContext(LanguageManager.wrap)` added where it was missing
  (`MainActivity`, `SoloLearningActivity`, and present on Splash,
  registration, both logins and the initialization screen) — before this,
  language selection silently had no effect on the landing and Solo screens.
- `language_switch_label` was swapped between files (Swahili file said
  "Language", English said "Lugha") — corrected to
  `Lugha` / `Language` / `اللغة`.
- **Parity: 170 keys in each of `values`, `values-en`, `values-ar`, with
  byte-identical key sets** (verified both by count and `diff` of sorted key
  lists). All 154 `R.string.*` keys used by the code exist in all three.
- Arabic rendering: `android:supportsRtl="true"` with
  `TEXT_DIRECTION_RTL` on the Arabic brand line.

---

## 4. Box 3 — Integration, branding & startup

### 4.1 Registration → domain architecture

`submitRegistration()` no longer paints `SUBMITTED` onto the record by hand.
It now:

1. Captures identity + administration into the `Madrassa` record, including
   `headUstadhId` (previously only kept in the step draft — but
   `RegistrationPolicy` requires it, and `RegistrationService.activate()`
   later reads it as the Head Ustadh's full name).
2. Runs `RegistrationService.submit(new RegistrationSubmission(madrassa,
   password))` on a dedicated worker thread — the service validates policy,
   enforces `CredentialPolicy.validNewPassword`, rejects duplicate names,
   hashes the password with **PBKDF2 (210k iterations)** and writes record +
   pending credential in **one SQLite transaction** (`RegistrationTransactionRepository`).
3. On success → Step 5 (Submitted).
4. On failure → maps `madrassa_name_exists`, `registration_save_failed`,
   `invalid_initial_password` and any policy code to the correct localized
   message, and **resets `approvalStatus` to `DRAFT` / `submittedAt` to 0**,
   because the service marks the in-memory record `SUBMITTED` before writing —
   without the reset a failed save would be refused by `canSubmit()` forever.
5. A `submitting` flag plus a disabled `Next` button prevents double
   submission; `onDestroy()` shuts the worker down.

Dependencies are constructed exactly like the login screens do:
`MadrassaRepository` + `PendingRegistrationCredentialRepository` +
`RegistrationTransactionRepository` + `Pbkdf2PasswordVerifier`.

### 4.2 Startup experience

- **`SplashActivity`** — the fixed `postDelayed(..., 1100)` sleep is gone.
  A background worker performs the real boot sequence while the entrance
  animation plays: open `EduNoorDatabase` (creating/migrating the schema),
  `MadrassaRepository.findAll()` (load real records), then read the stored
  approval status. Each step updates a live status line
  (`init_step_database` → `init_step_madrassa` → `init_step_ustadh_status`
  → `init_session_ready`). Navigation happens only when **both** the work and
  the 900ms designed entrance are finished — a fast device moves fast, a slow
  device is never cut off mid-load. Storage failures degrade to an honest
  message instead of a crash. `onDestroy()` cancels worker and callbacks.
- **`InitializationActivity` (new, spec §11)** — shown after successful
  authentication via `forUstadh(context, madrassaId)` /
  `forParent(context, parentId)`. It performs genuine per-step work (open
  store → load the Madrassa record → verify `ApprovalStatus.ACTIVE`, or for
  parents: load the parent → load the Madrassa → build the validated
  `ParentSession` snapshot) with the status text driven by that work, then
  reveals a branded welcome panel. Because no dashboard exists yet, it ends
  honestly on "the management dashboard arrives in the next stage"
  (`init_next_stage`) rather than faking a transition. Registered in
  `AndroidManifest.xml` (now **7 activities**).

### 4.3 Branding

Splash, initialization, landing, Solo, auth and registration all share the
same mark, walnut→clay gradient, gold brand line and palette tokens — the
startup and post-login experiences now read as one product.

---

## 5. Verification

### 5.1 Pre-build checklist (`docs/BUILD_REFERENCE.md` §5) — PASSED

| # | Check | Result |
|---|-------|--------|
| 1 | No stray pager dumps / `*.bak` in build path | Pass — 14 historical `.bak*` files preserved (filenames do not end in `.java`, so `javac` ignores them) |
| 2 | No zero-byte `.java` files | Pass — none |
| 3 | Every `R.string.*` key in all 3 locales | Pass — 154 used keys, 0 missing in `values`/`values-en`/`values-ar` |
| 4 | Manifest activities | Pass — 7: Main, Splash, SoloLearning, auth.parent, auth.ustadh, **Initialization**, registration |
| 5 | `ANDROID_HOME` + `local.properties` | Pass — `sdk.dir=/data/data/com.termux/files/home/android-sdk` |
| 6 | No reuse of `manual/v71/generated` | Pass — script wipes it |
| 7 | Manual first, Gradle second | Followed |

### 5.2 Canonical manual build — PASSED

```
BUILD COMPLETE
manual/v71/apk/Madrassa-EduNoor-v7.1-debug.apk   334K (341,223 bytes)
apksigner verify: Verifies
  v1 false · v2 true · v3 true · 1 signer
SHA-256: 3caee341fbf6ee7c502148adf3f7343e3fcb2807534ec36e851e0cdb70f6cf19
BUILD_EXIT: 0
```

This is the first build to include the new `InitializationActivity`, the
rewritten registration/login/splash flows and every Box 1–3 edit; `javac`
compiled all sources without error.

### 5.3 Gradle compile check — PASSED

```
> Task :app:compileDebugJavaWithJavac
BUILD SUCCESSFUL in 2m 43s
15 actionable tasks: 10 executed, 5 up-to-date
GRADLE_EXIT: 0
```

Notes: `compileDebugKotlin NO-SOURCE` (project is pure Java), only
deprecation notes — no warnings or errors from this round's code. The
manifest `package=` attribute warning is a pre-existing AGP 8 namespace
recommendation, not a failure. Gradle remains the secondary path (flaky
under proot/futex); the manual build above is the release gate. **Both
build paths are green.**

### 5.4 Additional invariants

- String parity: **170 / 170 / 170**, identical key sets (verified by `diff`).
- Hardcoded colors: **0** live `.java` files contain `Color.rgb`.
- Source control: **committed and pushed** — 172 files, +25,849 / −237
  landed in `32c12f7` (domain, auth, registration and UI layers),
  `c7a87b1` and `c607b2e` (this report), on top of `3b4da04` →
  `a53bcaa` → `3c70062`. `git ls-remote origin` confirms
  `refs/heads/main` equals local `HEAD`, and `git push` reports
  *Everything up-to-date*.

---

## 6. Bug inventory fixed this round

| # | Bug | Fix |
|---|-----|-----|
| 1 | Password visibility toggle swallowed all taps on the field | Toggle confined to a right-edge 46dp strip |
| 2 | 97dp of content clipped inside an 82dp card | Rows → `WRAP_CONTENT` + margins |
| 3 | 28 hardcoded `Color.rgb` values | All resolve `R.color.edunoor_*` |
| 4 | Fixed-height text clipped in AR/long translations | Heights → `WRAP_CONTENT` (min-height semantics) |
| 5 | `language_switch_label` swapped between languages | Corrected in all 3 files |
| 6 | Language selection had no effect on Main/Solo | `attachBaseContext(LanguageManager.wrap)` added |
| 7 | Tanzania-only phone message despite selectable country codes | Country-neutral `registration_invalid_phone` |
| 8 | Splash slept a fixed 1100 ms | Real init work + live status + dual completion gate |
| 9 | `InitializationActivity` missing from the manifest | Registered (7 activities) |
| 10 | Registration never called `RegistrationService` (manual `SUBMITTED`) | Domain submit, async, error-mapped |
| 11 | `madrassa.headUstadhId` never set → policy would reject submit | Written from the Head Ustadh name at capture |
| 12 | Failed save left record `SUBMITTED`, blocking retries | Reset to `DRAFT` + `submittedAt = 0` |
| 13 | Keyboard covered the region/district/country pickers | `hideKeyboard()` before each dialog |
| 14 | `EduNoorButton` still animated/clicked while disabled | Disabled guard swallows the press |
| 15 | Registration used native tinted `Button`s | `EduNoorButton.primary/secondary` |

---

## 7. Architecture boundaries respected

- **Local-first / SQLite only** — no network was introduced; registration,
  auth, splash and initialization all read/write the on-device store.
- **No new technologies** — plain `Activity` + programmatic views, existing
  `EduNoorDatabase`, existing domain services; no new libraries, no XML
  layouts, no architecture change.
- **UI → domain, never UI → SQL** — the registration screen talks to
  `RegistrationService`; repositories are only constructed as the service's
  adapters, mirroring the login screens' wiring.
- **Main-thread safety** — every PBKDF2 path (login, registration) runs on a
  single-thread executor; splash/initialization I/O runs on their workers.
- **Existing assets preserved** — 14 `.bak*` files and all prior commits are
  untouched.

---

## 8. Honest limitations & open items

1. **Dashboard does not exist yet** — `InitializationActivity` states this
   plainly (`init_next_stage`) instead of pretending to navigate somewhere.
2. **Parent accounts are not provisionable from the UI yet** — parent login
   will correctly return `INVALID_CREDENTIALS` until a provisioning flow
   creates them.
3. **Second-device install/launch still open** — no adb device answered at
   the last three known addresses; a fresh wireless pairing is needed before
   the install + test checkpoint (`BUILD_REFERENCE.md` §5 still lists this).
4. **Gradle path is environmental** — this run succeeded, but it stays
   flaky under proot/futex; the manual
   build is the release gate.

---

## 9. Files changed (this round)

**New**
- `app/src/main/java/com/zamcan/madrassa/InitializationActivity.java` (547
  lines) — spec §11 branded initialization.

**Rewritten / heavily edited**
- `registration/MadrassaRegistrationActivity.java` (1890) — palette,
  responsive rows, `EduNoorButton` nav, field modes, keyboard handling,
  domain submission.
- `auth/parent/ParentLoginActivity.java` (625),
  `auth/ustadh/UstadhLoginActivity.java` (660) — design system, async auth,
  full result handling, `InitializationActivity` hand-off.
- `SplashActivity.java` (449) — real startup work + status line.
- `ui/components/FloatingOutlineField.java` (652) — toggle fix, persistent
  label, modes, IME actions.
- `ui/components/EduNoorButton.java` (175) — disabled guard.
- `ui/components/EduNoorCard.java`, `EduNoorStateView.java`,
  `EduNoorProgressView.java` — responsive rows.
- `MainActivity.java`, `SoloLearningActivity.java` — language wrap +
  responsive header/hero/progress.

**Resources**
- `res/values/strings.xml` + `values-en` + `values-ar` — 19 new keys,
  label fix, country-neutral phone message (170/170/170).
- `res/values/dimens.xml` — spacing/type tokens.
- `res/values/colors.xml` — single palette (unchanged this round, now
  consumed everywhere).
- `AndroidManifest.xml` — `InitializationActivity` registered.

---

## 10. Recommended next steps

1. Second-device install/launch verification (fresh adb pairing) — still
   blocked; no device answered at the last three known addresses.
2. Build the management dashboard and point `InitializationActivity`'s
   end state at it.
3. Add parent provisioning so parent login can be exercised end-to-end.
