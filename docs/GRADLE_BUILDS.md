# Madrassa EduNoor — Build History and Gradle Guide

Companion: `docs/BUILD_REFERENCE.md` is the short operational
reference for every future build; this file keeps the full history.
Verified 2026-09-21: 128 strings in each of `values/values-en/values-ar`,
109 distinct `R.string.*` keys used, zero 0-byte `*.java` files,
`build-tools/34.0.0` only vs `compileSdk 35` (AGP may request a
`build-tools;35.0.0` download on Gradle path).

## 1. Successful build history

### V6 — approved visual foundation (tag `v0.6.0`, commit `a53bcaa`)
- Date: 2026-09-15
- Scope: `MainActivity.java` + `SplashActivity.java` only, no
  `domain/`, `data/repository/`, `auth/`, `registration/`,
  `SoloLearningActivity`.
- Resources: drawables (`edunoor_*`, `parent_card`, `ustadh_card`),
  `values/`, `values-en/`, `values-ar/` with base strings only.
- Method: **manual toolchain**, not Gradle:
  `aapt2 compile` -> `aapt2 link` -> `javac -source 8 -target 8`
  -> `d8 --min-api 26` -> python zip add `classes.dex`
  -> `manual/align_*.py` (4096-byte alignment)
  -> `apksigner` v1+v2+v3 with the local Android debug keystore.
- Outputs in `manual/apk/`:
  `Madrassa-v6-unsigned.apk` (33K),
  `Madrassa-v6-aligned-unsigned.apk` (99K),
  `Madrassa-EduNoor-v6-debug.apk` (102K, signed, verified).
- Gradle was present (`AGP 8.6.1`, Kotlin `2.0.21`,
  `compileSdk/targetSdk 35`, `minSdk 26`) but not used for the
  installable artifact. No `gradlew` wrapper in repo.

### V7.1 — manual build recovered (2026-09-21)
- Previous state: `manual/v71/` contained only `base-unsigned.apk`
  (21K) + stale `R.java` (2026-09-15) + only `R*.class`, empty `dex/`.
  `build_v71.sh` aborted at step `[4/8] javac`.
- Outputs after fix in `manual/v71/apk/`:
  `Madrassa-EduNoor-v7.1-debug.apk` (306K, v1+v2+v3 verified),
  plus unsigned/aligned intermediates and `.idsig`.
- Re-verified 2026-09-21 14:55 UTC after server restart:
  `BUILD COMPLETE`, 306K, `Verifies`, v1+v2+v3 true,
  SHA-256 `0f7429e724af84f125ae8bfdf4031a5bbacb0314cd013e6f5d0a0acd793437ea`.

## 2. What blocked the current build

1. **Stale `R.java` / new resources.**
   `app/src/main/res/values*/strings.xml` gained ~320 lines on
   2026-09-18 (Solo Learning + Registration strings) after the last
   `aapt2 link`. `manual/v71/generated/.../R.java` was still dated
   2026-09-15, so `javac` failed with `cannot find symbol R.string.*`
   for ~20 Solo keys and ~50 Registration keys.
   Fix: re-run full `aapt2 compile` + `aapt2 link` before `javac`;
   do not reuse `manual/v71/generated` across resource edits.

2. **Empty `TanzaniaGeographyDataset.java` (0 bytes).**
   Referenced by `GeographyDatasetRegistry.register(new ...)`.
   Manual script skips it (`-size +0c`) so D8 succeeded without TZ data,
   but Gradle/`javac` without the filter sees an empty compilation unit.
   Fix: implemented minimal `GeographyDataset` stub (`TZ`, empty roots,
   `COUNTRY/REGION/DISTRICT/WARD/LOCALITY` levels) at
   `app/src/main/java/com/zamcan/madrassa/data/geography/TanzaniaGeographyDataset.java`.

3. **`MadrassaStore` interface mismatch.**
   `domain/repository/MadrassaStore.java` declared
   `void save(...)` / `void updateApprovalStatus(...)`, while
   `data/repository/MadrassaRepository.java` implemented `boolean`
   versions and `domain/registration/RegistrationService.java`
   used them as `boolean updated = ...`.
   `javac` errors: `return type boolean is not compatible with void`,
   `void cannot be converted to boolean` (lines 192, 238, 302).
   Fix: changed interface to `boolean save(...)` and
   `boolean updateApprovalStatus(...)`.

4. **`build_v71.sh` used `$HOME/Madrassa`.**
   In this container `HOME=/root`, real project is
   `/data/data/com.termux/files/home/Madrassa`, and SDK is
   `/data/data/com.termux/files/home/android-sdk`, so
   `ANDROID_JAR=$HOME/android-sdk/...` did not exist
   (`package android.app does not exist` when reproduced).
   Fix: derive `PROJECT` from script dir, prefer `ANDROID_HOME`,
   fallback to Termux SDK path. Always export:
   `ANDROID_HOME=/data/data/com.termux/files/home/android-sdk`.

5. **Gradle SDK location missing.**
   `gradle :app:assembleDebug` failed in 1m38s with:
   `SDK location not found. Define ... ANDROID_HOME ... or sdk.dir ...`.
   Fix: created `local.properties` with
   `sdk.dir=/data/data/com.termux/files/home/android-sdk`
   (untracked, local-only). Re-run with `ANDROID_HOME` exported.

6. **Stray files.**
   Deleted pager dumps `"= ["` and `"trip() + ..."`, plus
   `MadrassaRegistrationActivity.java.bak` (0 bytes) and `.bak2`.
   They are not compiled but pollute `git status` and confuse builds.

## 3. Reproducible builds

### A. Manual build (canonical, proven on-device path)
```bash
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
bash manual/build_v71.sh
ls -lh manual/v71/apk/Madrassa-EduNoor-v7.1-debug.apk
apksigner verify --verbose manual/v71/apk/Madrassa-EduNoor-v7.1-debug.apk
```
Steps inside: clean `manual/v71`, check `aapt2/d8/javac/apksigner`
+ `android.jar`, `aapt2 compile`, `aapt2 link --java`, `javac`,
`d8 --min-api 26`, zip `classes.dex` (STORED), `align_apk.py`
(4096), `apksigner --v1 --v2 --v3`, verify + `sha256sum`.
Per product rule, install the resulting `-debug.apk` on a second
Android device before next major stage.

### B. Gradle build
```bash
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export HOME=/data/data/com.termux/files/home  # caches live here, not /root
gradle :app:assembleDebug
ls app/build/outputs/apk/debug/
```
Requirements:
- `local.properties` (`sdk.dir=...`) — do not commit.
- System Gradle 9.7.1 + Java 21.0.12; project pins AGP 8.6.1 +
  Kotlin 2.0.21, `compileSdk/targetSdk 35`, `minSdk 26`,
  `kotlinOptions.jvmTarget=21` + `compileOptions` Java 21
  (aligned 2026-09-21), `android.useAndroidX=true`.
- No `gradlew`; uses Termux `/usr/bin/aapt2` via
  `android.aapt2FromMavenOverride` (experimental warning is expected).
- Status 2026-09-21: SDK + `compileOptions` fixes applied.
  Full `:app:assembleDebug` was attempted repeatedly but could not
  finish inside this session: AGP/Kotlin dependency download exceeds
  the 2–5 min execution window and background workers were killed by
  server restarts (`gradle-build*.log` show daemon startup only).
  No Gradle code error remains visible; the remaining risk is
  version skew: AGP 8.6.1 officially supports Gradle 8.7–8.10, not
  Gradle 9.x. Until proven otherwise treat **manual build (A) as
  canonical** for releases. Next Gradle attempt:
  1. `gradle wrapper --gradle-version 8.10.2` (one slow run),
  2. `./gradlew :app:assembleDebug` with `ANDROID_HOME` exported,
  3. if green, commit the wrapper; if red, paste the first
     `What went wrong:` block into this file before changing code.

## 4. Pre-build checklist (next time)
1. `git status --short` clean (no `= [`, `trip()...`, `*.bak`).
2. No `*.java` is 0 bytes:
   `find app/src/main/java -name '*.java' -size 0c -print` must be empty.
3. `grep -rho 'R.string.[A-Za-z0-9_]*' app/src/main/java | sort -u`
   every key exists in `values/strings.xml` **and** `values-en`,
   `values-ar` (trilingual parity).
4. `AndroidManifest.xml` lists every `Activity`
   (`Main`, `Splash`, `SoloLearning`, `auth.parent`, `auth.ustadh`,
   `registration`).
5. `export ANDROID_HOME=...` + `local.properties` present.
6. Delete `manual/v71` or let script `rm -rf` it; never reuse stale `R.java`.
7. Run manual build first; keep Gradle as secondary until wrapper pinned.
8. Record APK name, size, `sha256sum`, `apksigner verify`, second-device result.

## 5. Recovery verification — 2026-09-21 14:54 UTC (post-restart)
- Re-verified 2026-09-21 15:06 UTC: `BUILD COMPLETE`, 306K, `Verifies`, v1+v2+v3 true, SHA-256 `0f7429e724af84f125ae8bfdf4031a5bbacb0314cd013e6f5d0a0acd793437ea` (identical artifact, `manual/v71/apk/Madrassa-EduNoor-v7.1-aligned-unsigned.apk` 313388 bytes, alignment OK).
- Rebuilt 2026-09-21 18:25 UTC with SDK-version fix (`--min-sdk-version 26 --target-sdk-version 35` in `manual/build_v71.sh`): `BUILD COMPLETE`, 273K, `Verifies`, v2+v3 true, SHA-256 `adc909b843e599d27d9cbf6f3b0d943003a81a0550acb3dddaa261730a418abe`, `targetSdkVersion:'35'`. ADB pair `10.184.101.172:45217` + install to `10.184.101.172:36449` (SM_A075F): `Success`; `SplashActivity` launches, pid confirmed. Root cause of prior install failure was missing SDK versions (targetSdk 0).
- Rebuilt 2026-09-21 18:59 UTC (V7.2 landing + inputs): `BUILD COMPLETE`, 286K, `Verifies`, v2+v3 true, SHA-256 `07872cf6fa6546fc126d5bd09931a71afedb2db4dbcf1025a81ae4ab221807e7`. Install to SM_A075F: `Success`; `SplashActivity` launches, pid 24753. Changes: ScrollView canvas, unified 68dp cards with `solo_card.xml` (teal), `solo_card_title` = "Solo Learning", 11sp floated labels, `+255` prefix + example hints, password toggle + strength/missing-part feedback, phone normalize/validate, trilingual parity for 11 new strings.

- Rebuilt 2026-09-22 (V7.3 location + country-code batch): `BUILD COMPLETE`, 314K, `Verifies`, v2+v3 true, SHA-256 `d04b0675e1653ac7e971e37c10e7d0b6c9ea0a606a9f8d6bc0f18dc8ad87c2b0` (log `/tmp/opencode/manual-v73.log`). Changes:
  1. `data/geography/TanzaniaGeographyDataset.java` — real offline dataset replacing the empty stub: all 31 regions (26 mainland + 5 Zanzibar) with district lists; `WARD`/`LOCALITY` intentionally return no children and fall back to free text.
  2. `MadrassaRegistrationActivity` — region → district cascading pickers backed by `LocalGeographyRepository` (tap opens a single-choice list, `▾` indicator, choosing a region clears district + ward); district falls back to typing if a region has no list; picker mode disabled entirely when a country has no dataset.
  3. Country codes — `Country.dialCode` (`+255/+254/+256/+20`), tappable prefix on both phone fields, `EduNoorRules.normalizePhone(dial, raw)` / `validPhone(dial, raw)` (the Tanzanian methods now delegate to `+255`, so auth is unchanged; Egypt validated as 10 digits, others 9).
  4. Step restore — Back/Next no longer empties the form: identity, administration and password drafts are written back into the rebuilt fields; `captureIdentity` no longer stores the review placeholder `—`; optional secondary phone / email / masjid fields no longer force a phantom `—` or a false invalid-phone error.
  5. `required()` now shows the specific `registration_enter_*` message instead of failing silently; `registration_invalid_email` / `registration_invalid_count` wired.
  6. Adaptive launcher icon `mipmap-anydpi-v26/ic_launcher(_round).xml` (background `edunoor_background`, foreground + monochrome `edunoor_icon`) referenced by the manifest; header language toggle live in `MainActivity`.
  7. Trilingual parity 146/146/146 keys (5 new `registration_select_*` / `field_example_ward` keys in base/en/ar).
  Install to the second device **pending**: `adb connect` to the previously paired addresses (`10.184.101.172:36449`, `10.184.101.172:45217`, `10.214.48.67:45401`) returned no device on 2026-09-22 — a fresh wireless pairing is required before the second-device test required by the product rule.

- Gradle verification of the V7.3 sources: `gradle :app:assembleDebug` → **BUILD SUCCESSFUL in 8m 5s**, `GRADLE_EXIT:0` (log `/tmp/opencode/gradle-v73.log`). Manual artifact SHA-256 `d04b0675e1653ac7e971e37c10e7d0b6c9ea0a606a9f8d6bc0f18dc8ad87c2b0`.

- V7.4 source round (professional UI/UX directive) — **compile-verified, no APK built yet** (directive: no APK unless explicitly requested): `gradle :app:compileDebugJavaWithJavac` → BUILD SUCCESSFUL, exit 0, twice (before and after the login scroll wrap). Changes:
  1. `EduNoorCard.gateway(...)` — single shared gateway-card geometry for Solo Learning / Parent / Ustadh: one padding system, one type scale, start-aligned text (Arabic RTL mirrors), `minHeight` read from `R.dimen.role_card_height` instead of fixed row heights, so longer translations and larger font scaling grow the card instead of clipping it.
  2. `MainActivity.soloCard()` / `roleCard()` now delegate to that component — ~266 duplicated lines removed; click routing (Solo → learning area, Parent/Ustadh → their logins) and the visual identity (drawables + accent colours) unchanged.
  3. `Gravity.LEFT` → `Gravity.START` everywhere on the landing header; header language chip 32×44dp → 48×48dp; utility bar items 44dp → 48dp minimum touch height.
  4. `dimens.xml`: `role_card_height` reconciled 64dp → 68dp (was a second, conflicting source of truth); the dimen is now actually referenced.
  5. `AndroidManifest.xml`: `android:windowSoftInputMode="adjustResize"` on registration and both logins; Parent/Ustadh login content wrapped in a `ScrollView` (they had none) so the focused field and primary button stay reachable when the keyboard opens.
  6. Bottom-bar dialog copy (`privacy_message`, `terms_message`, `help_message`, `dialog_ok`) moved from hardcoded English literals into `strings.xml` ×3; `SoloLearningActivity` uses `dialog_ok` too. String parity now 150/150/150.
  Static checks after the round: `git diff --check` clean, brace/paren balance 0 on every touched file, XML well-formed, `R.string` used-but-missing = 0, no `Gravity.LEFT`/hardcoded `"OK"` left in Java.
  **The last built APK (V7.3, SHA above) predates this round** — rebuild (`bash manual/build_v71.sh`) is required before the pending second-device install.

- Manual `bash manual/build_v71.sh` with `ANDROID_HOME=/data/data/com.termux/files/home/android-sdk`: **BUILD COMPLETE**.
- Outputs in `manual/v71/apk/`: `Madrassa-EduNoor-v7.1-debug.apk` (306K), aligned-unsigned (307K), unsigned (239K), base (38K), plus `.idsig`.
- `apksigner verify --verbose`: v1 true, v2 true, v3 true (v3.1/v4 false — expected for debug keystore).
- SHA-256: `0f7429e724af84f125ae8bfdf4031a5bbacb0314cd013e6f5d0a0acd793437ea`.
- Fixes verified in this pass:
  1. Trilingual parity restored: 22 `solo_*` keys added to `values-en` and `values-ar` (was 22 missing each; now 0 missing). Build still links via fallback, but parity is required for premium Kiswahili/English/Arabic-RTL product standard.
  2. `LOCAL_DATABASE_MIGRATIONS.md` extended for v8 (programmes nullable), v9 (content_shares), v10 (student groups + flexible class_groups). `DATABASE_VERSION = 10` is correct — do not revert to 7.
  3. Prior fixes confirmed intact: `MadrassaStore` boolean signatures, `TanzaniaGeographyDataset` stub (899 bytes), zero 0-byte Java files, `ProgressState` deprecated bridge to canonical `ProgressStatus`, `TenantPolicy` single enforcement point, `AcademicCoreService` (927 lines) behind repository contracts.
- Gradle `assembleDebug`: not used for release; system Gradle 9.x exceeds AGP 8.6.1 support and times out on-device (>240s). Manual toolchain remains canonical until a wrapper pins Gradle 8.7–8.10.
- Design professionalism: V6 landing foundation untouched; Solo/Auth/Registration activities use `R.string` resources (no hardcoded `setText("...")`); drawables (`edunoor_*`, `parent_card`, `ustadh_card`) preserved; next step is second-device install test per product rule before next batch.
