# Madrassa EduNoor — Build Reference (Canonical)

Date: 2026-09-21
Project: `/data/data/com.termux/files/home/Madrassa`
Authority: `docs/GRADLE_BUILDS.md` + this file. This file is the short
operational reference for all future builds. `GRADLE_BUILDS.md` keeps history.

## 1. Background-task audit (2026-09-21)

Active processes checked with `ps aux | grep gradle` after server restart:

- Before restart (from logs + prior `ps`):
  - `timeout 240 gradle :app:assembleDebug` + Gradle client (PID 13967/13969)
  - wrapper bash `gradle :app:assembleDebug > /tmp/opencode/gradle-build3.log`
    (PID 14375/14376) — interrupted by restart, `gradle-build3.log`
    contains only daemon-startup lines, no result.
  - 2x Gradle daemons (Java 21, `-Xmx1024m`) — killed by restart.
- After restart:
  - No surviving daemons. 6 stale busy daemons reported by Gradle
    (`6 busy Daemons could not be reused`) — they are unreachable,
    Gradle starts a new daemon automatically. Harmless.
  - Current: `gradle :app:assembleDebug > /tmp/opencode/gradle-build4.log`
    (background `sh_0c47fa7c`). See §4 for result.
- `.kilo/`: only `kilo.jsonc` + `.gitignore`, no tasks.
- No other opencode background jobs for Madrassa remain.

Conclusion: no orphan builds to kill. All former runs are accounted for
in `/tmp/opencode/gradle-build*.log` + `/tmp/opencode/gradle-v72.log`.

## 2. Premium Islamic design — polishing / enforcement status

Enforced, verified 2026-09-21:

- Palette single source: `app/src/main/res/values/colors.xml`
  (`edunoor_background #F3E8D3`, `surface #FFF9ED`, `walnut/ink #3E2920`,
  `clay #A95F3D`, `gold #C69A45`, `border #D8C5A8`, etc.).
- Theme: `values/styles.xml` `AppTheme` (Material Light NoActionBar,
  status/nav = `edunoor_background`, accent = `edunoor_clay`).
- Components: `ui/components/EduNoorCard, EduNoorButton,
  EduNoorProgressView, EduNoorStateView, FloatingOutlineField`
  all resolve colors via `context.getColor(R.color.edunoor_*)`.
- `MainActivity.java`:
  - Solo Learning card (`soloCard()` via `EduNoorCard.create`) placed
    above Parent/Ustadh gateways (offline-first rule, comment `3F-A6`).
  - Parent/Ustadh cards now launch real
    `auth.parent.ParentLoginActivity` / `auth.ustadh.UstadhLoginActivity`
    (was Toast placeholders).
  - Language dialog uses `LanguageManager.get/setLanguage` +
    `setSingleChoiceItems` + `recreate()` (was Toast stub).
  - `root.setBackgroundColor(background)` uses
    `getColor(R.color.edunoor_background)` — not hardcoded.
- `SplashActivity.java`: `attachBaseContext` wraps with
  `LanguageManager.wrap(newBase)` — RTL/Arabic path intact.
- Drawables `edunoor_*`, `parent_card`, `ustadh_card` present.
- `grep R.string` = 114 used keys, all present in `values`,
  `values-en`, `values-ar` (128 keys each, trilingual parity OK).

No design regression. No fix needed.

## 3. Failures fixed (why each build failed before)

| # | Symptom | Cause | Fix (still applied, verified) |
|---|---------|-------|-------------------------------|
| 1 | `javac: cannot find symbol R.string.*` (~70 Solo+Registration keys) | `strings.xml` grew ~320 lines 2026-09-18, `manual/v71/generated/R.java` stale from 2026-09-15 | Never reuse `manual/v71/generated`; `build_v71.sh` does `rm -rf $BUILD` + fresh `aapt2 compile` + `aapt2 link --java` |
| 2 | Empty compilation unit / missing TZ data | `TanzaniaGeographyDataset.java` 0 bytes, referenced by `GeographyDatasetRegistry` | Stub implemented (`TZ`, empty roots, `COUNTRY/REGION/DISTRICT/WARD/LOCALITY`); verified 899 bytes, non-zero |
| 3 | `return type boolean is not compatible with void`, `void cannot be converted to boolean` | `domain/repository/MadrassaStore.java` declared `void save/updateApprovalStatus`, impl + `RegistrationService` use `boolean` | Interface changed to `boolean save(...)`, `boolean updateApprovalStatus(...)`; verified |
| 4 | `package android.app does not exist` in manual repro | `build_v71.sh` used `$HOME/Madrassa` (`HOME=/root`), real project is Termux path | Script derives `PROJECT` from script dir, prefers `ANDROID_HOME`, fallback Termux SDK; always export `ANDROID_HOME=/data/data/com.termux/files/home/android-sdk` |
| 5 | `SDK location not found` (Gradle, 1m38s, `gradle-build.log`) | No `ANDROID_HOME` in daemon env + no `local.properties` | Created untracked `local.properties` (`sdk.dir=/data/data/com.termux/files/home/android-sdk`); export `ANDROID_HOME` + `ANDROID_SDK_ROOT` every run |
| 6 | `Internal Error waitBarrier_linux futex FUTEX_WAKE ENOSYS` (`gradle-v72.log`, Java 21.0.12, proot) | Java 21 + Gradle daemon under PRoot/Termux without futex support | Environmental, not code. Manual build unaffected. For Gradle prefer `--no-daemon` or retry; do not treat as code bug |
| 7 | Stray files confuse `git status` | Pager dumps `"= ["`, `"trip() + ..."`, `MadrassaRegistrationActivity.java.bak/.bak2` (0 bytes) | Deleted; pre-build check §5.1 enforces |

## 4. Build retry results (2026-09-21)

### A. Manual build — CANONICAL, SUCCESS
```bash
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
bash manual/build_v71.sh
```
Result: `BUILD COMPLETE`, exit 0.
- `manual/v71/apk/Madrassa-EduNoor-v7.1-debug.apk` 306K
- `apksigner verify`: `Verifies`, v1+v2+v3 `true`
  (v3.1/v3.2/v4 false = expected, not enabled)
- `SHA-256: 0f7429e724af84f125ae8bfdf4031a5bbacb0314cd013e6f5d0a0acd793437ea`
  — identical to 2026-09-21 14:55 UTC re-verification. Stable/reproducible.
- `javac` warnings only: `bootstrap class path not set`, `source/target 8
  obsolete`, `deprecated API` — harmless, expected with `-source 8`.

### B. Gradle build — SUCCESS (2026-09-21)
```bash
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
gradle :app:assembleDebug   # log: /tmp/opencode/gradle-build4.log
```
Result: `BUILD SUCCESSFUL in 6m 29s`, `GRADLE_EXIT:0`,
`31 actionable tasks: 10 executed, 21 up-to-date`.
- `app/build/outputs/apk/debug/app-debug.apk` 943K
- `apksigner verify`: `Verifies`, v2 `true` (debug key, v1 false =
  expected AGP debug signing default)
- `SHA-256: 1e2eb024cbab03032d56383132cc2954ce2888e758c7fa0bef8e0acd448619a4`
- Notes: Gradle 9.7.1 + AGP 8.6.1 worked despite version-skew risk;
  experimental warning `android.aapt2FromMavenOverride=.../usr/bin/aapt2`
  is expected; `6 busy Daemons could not be reused` after restart is
  harmless. Prior risks (build-tools 35, futex crash, network deps)
  did not block this run.

## 5. Pre-build checklist (run every time)

1. `git status --short` — no `= [`, `trip()...`, `*.bak`.
2. `find app/src/main/java -name '*.java' -size 0c -print` — must be empty.
3. `grep -rho 'R.string.[A-Za-z0-9_]*' app/src/main/java | sort -u` —
   every key in `values`, `values-en`, `values-ar`.
4. `AndroidManifest.xml` lists `Main, Splash, SoloLearning,
   auth.parent, auth.ustadh, registration` (all 6 present now).
5. `export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk`
   (+ `ANDROID_SDK_ROOT`), `local.properties` present (untracked).
6. Never reuse `manual/v71/generated`; script wipes it.
7. Manual first, Gradle second.
8. Record APK name, size, `sha256sum`, `apksigner verify --verbose`,
   second-device install result.

## 6. Plea — both builds successful, prior failures explained

PLEA: BUILD SUCCESSFUL on both paths.

- Manual (canonical): `BUILD COMPLETE`, 306K, v1+v2+v3 verified,
  SHA-256 `0f7429e7...437ea` (reproducible, matches prior verification).
- Gradle: `BUILD SUCCESSFUL in 6m 29s`, `GRADLE_EXIT:0`,
  `app-debug.apk` 943K, `Verifies` (v2 true),
  SHA-256 `1e2eb024...619a4` (log `/tmp/opencode/gradle-build4.log`).

All current code errors are solved
(zero-byte files none, `MadrassaStore` boolean match, trilingual parity,
manifest complete, SDK path fixed).

Prior failures were because of: stale `R.java` after resource growth;
empty `TanzaniaGeographyDataset`; `MadrassaStore` void-vs-boolean mismatch;
script `$HOME` vs Termux SDK path; missing `ANDROID_HOME`/`sdk.dir` for
Gradle; stray pager/`.bak` files; and Termux/PRoot environment limits
(Java futex crash, Gradle 9 vs AGP 8.6, experimental `aapt2` override,
missing `build-tools;35.0.0`). None remain as code blockers — remaining
Gradle risks are environmental/version-pinning only.

## 7. V7.3 batch — location + country-code logic (2026-09-22)

Resumed from the interrupted request round ("proceed now with build …
language toggle, locations in registrations, country codes, validation
messages, icon"). Everything from that round is now implemented and
built.

Manual build (canonical): `BUILD COMPLETE`, `manual/v71/apk/
Madrassa-EduNoor-v7.1-debug.apk` 314K, `apksigner verify: Verifies`
(v2+v3 true), SHA-256
`d04b0675e1653ac7e971e37c10e7d0b6c9ea0a606a9f8d6bc0f18dc8ad87c2b0`.

Delivered:

| Area | What changed |
|------|--------------|
| Geography data | `TanzaniaGeographyDataset` now ships 31 real regions + district lists (was a 0-content stub); ward/locality stay free text by design |
| Registration location | Region → district cascading pickers (`LocalGeographyRepository`), region change clears district/ward, `▾` affordance, free-text fallback when a level has no data |
| Country codes | `Country.dialCode`, tappable `+255 ▾` prefix on both phone fields, `EduNoorRules.normalizePhone/validPhone(dial, …)`; auth keeps `+255` semantics |
| Form integrity | Back/Next restores identity, administration and password drafts; review placeholder `—` never captured; optional secondary phone no longer fails validation |
| Validation UX | `required()` says which field is missing; email/count/phone messages live in all three languages |
| Launcher icon | Adaptive `mipmap-anydpi-v26/ic_launcher(_round).xml` wired in the manifest (vector foreground, parchment background, monochrome) |
| Strings | 146 keys in each of `values`, `values-en`, `values-ar` — parity holds |

Pre-build checklist §5 passed before the build: no stray/0-byte files,
all `R.string.*` keys present trilingually, 6 activities in the manifest,
`local.properties` + `ANDROID_HOME` set.

Still open: **second-device install/launch** — no adb device answered at
the last three known addresses, so a fresh wireless pairing is needed
before the product-rule install + test + git checkpoint.

§7.1 — V7.4 source round (compile-verified, **no APK built**):

The professional UI/UX directive round landed afterwards and was verified
with `gradle :app:compileDebugJavaWithJavac` (BUILD SUCCESSFUL, exit 0)
only, since the directive forbids building an APK unless requested. It
unifies the three landing gateway cards behind `EduNoorCard.gateway()`
(start-aligned/RTL-correct text, wrapping rows, `R.dimen.role_card_height`
as the single height source), raises header/utility touch targets to 48dp,
adds `adjustResize` + `ScrollView` to both login screens, and localizes
the bottom-bar dialog copy (string parity now 150/150/150). The V7.3 APK
above therefore **predates this round**: run `bash manual/build_v71.sh`
before the second-device install.

