# EduNoor Madrassa — Dev Credentials & Handover

> **DEV ONLY.** The demo data below is seeded by
> `app/src/main/java/com/zamcan/madrassa/core/dev/DevSeed.java`
> on first launch (Splash → “Kuandaa mfano wa majaribio”).
> Flip `DevSeed.ENABLED` to `false` before any production build.

## Sample logins (seeded automatically on fresh install)

### Madrassa (Ustadh / head) login — `UstadhLoginActivity`
| Field | Value |
|---|---|
| Identifier | `Madrassa Ya Kioo` (the madrassa name) — or the registered phone `0712 000 111` |
| Password | `Salama@2026` |

Tip: on the login screen, **long-press the screen title** to auto-fill
the demo credentials.

### Parent login — `ParentLoginActivity`
| Field | Value |
|---|---|
| Phone | `0713 000 222` (any format: `+255713000222`, `255713000222`, `0713000222`) |
| Password | `Amana2026` |

Same tip: long-press the title to auto-fill.

## What the seed contains (all through the real provisioning chain)

| Record | ID | Notes |
|---|---|---|
| Madrassa | `MAD-DEV-001` | Dar es Salaam, ACTIVE, registered phone |
| Head ustadh + credential | `UST-DEV-001` | PBKDF2-hashed, `first_login=false` → straight to dashboard |
| Parent + credential | `PAR-DEV-001` | linked to the madrassa |
| Class | `CLS-DEV-001` | “Darasa la Kwanza”, taught by the head ustadh |
| Student | `STU-DEV-001` | “Amina Binti Amana”, Qur'an level 4, Hifz level 2 |
| Programme/Course/Unit/Lessons | `PRG/CRS/UNT/LSN-DEV-*` | full academic spine |
| Fees | `FEE-DEV-001/002` | one UNPAID (madeni), one PAID |
| Progress | `PRG-DEV-L1/L2` | COMPLETED + IN_PROGRESS rows |

Dashboards reached after login: **RoleDashboardActivity → RoleListActivity**
(students with class/Qur'an/Hifz levels, classes, fees with status chips,
progress by student). The bundled Deen + Learning assets are reachable from
inside both dashboards and work fully offline.

## Build & run (Termux / second device)

1. **Pull**
   ```bash
   git clone https://github.com/ZamCan/Madrassa-.git
   cd Madrassa-
   ```

2. **Environment** — JDK 17+ and an Android SDK with `platforms;android-35`
   + `build-tools;35.0.0`:
   ```bash
   export JAVA_HOME=/path/to/jdk
   export ANDROID_HOME=/path/to/android-sdk
   ```

3. **Build**
   ```bash
   ./gradlew assembleDebug
   # APK: app/build/outputs/apk/debug/app-debug.apk
   ```
   If `aapt2` from the Maven cache will not execute on your host (this
   happens on some Linux environments/Termux), add the override the
   sandbox used:
   ```bash
   ./gradlew assembleDebug \
     -Pandroid.aapt2FromMavenOverride=$ANDROID_HOME/build-tools/35.0.0/aapt2
   ```

4. **Install & first launch** — install the APK, open the app; the splash
   seeds the demo madrassa, then log in with the credentials above.
   Language switch (SW/EN/AR + RTL) is the chip at the top-right of the
   landing page; the Kaaba mark beside it opens the Deen panel.

## Engineering notes for the coming servers/gateways

- All reads/writes already flow through `domain/repository/*Store`
  interfaces implemented by `data/repository/*` over SQLite
  (`EduNoorDatabase`, schema v10, 33 tables). A server-backed
  implementation only needs new repository implementations — no UI churn.
- Credentials: PBKDF2-HmacSHA256 (210k iterations), salted, per-account
  rows in `credentials` with `first_login` gating the password-change
  flow (`CredentialPolicy`).
- Assets (Qur'an audio, illustrations, UI sounds, calendar/salat engines)
  are APK-bundled by design: zero network, zero DB coupling.
