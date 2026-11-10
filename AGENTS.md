# EduNoor engineering instructions

This repository is the native Android Java, SQLite-first Madrassa — EduNoor application. Treat the README's explicitly incomplete features as incomplete until implementation and tests prove otherwise.

## Non-negotiable engineering rules
- Preserve the existing Android/Java architecture, Gradle build, SQLite schema and migrations. Inspect before modifying.
- Do not replace working engines with mocks, fake dashboard records, simulated payments, or placeholder audio.
- Default language Swahili; support English and Arabic RTL, including correct diacritics and accessible font scaling.
- Parent and Ustadh identities must be isolated. Enforce tenant and parent-child authorization at repository/service boundaries, not only in UI.
- Keep data offline-first. Any online service requires explicit consent, documented licensing, safe caching, and meaningful offline fallback.
- Keep the UI calm and premium: parchment, walnut, clay and restrained antique gold; use native responsive layouts, avoid excessive cards and verbose instructions.
- Never claim authentic Quran text, recitation, tafsir or adhan without verified provenance and redistribution permissions.
- Never claim production ready or independently certified without recorded build, test, device and security evidence.

## Execution sequence
1. Baseline: inspect source, build configuration, tests, and CI; record current behavior and failures.
2. Islamic content: implement a versioned, checksum-verified Quran content importer and provenance manifest. Validate surah count=114, verse totals and per-surah verse indexes against an independently verified source before displaying completeness.
3. Audio: use a real playback engine with lifecycle-aware play/pause/seek, queue, ayah repeat, continuous surah progression, audio focus, interruption recovery, and offline cache integrity. Separate Arabic recitation from translated audio and tafsir audio.
4. Salah: validate calculation methods, timezone/DST, high-latitude edge cases, permissions, notification channels, and actual licensed Arabic adhan playback. Provide sensor accuracy and calibration for Qibla, plus fallback to numerical bearing.
5. Academic workflows: implement authorized attendance, lessons, assessments, fees and payment-proof review with transactional SQLite operations and audit records. NIMELIPA is a payment claim, not automatic proof of payment.
6. UX: complete Solo Learning, Parent and Ustadh journeys, onboarding, loading/empty/error states, RTL, TalkBack and small-screen usability.
7. Verification: run unit tests, instrumentation tests, clean debug/release builds, lint, dependency/security checks, and tests on a second physical device. Record failures honestly.

## Acceptance gates
- All user-visible actions must invoke real implementations; disable unavailable features with an honest explanation.
- Quran text and audio assets require source/license/sha256 metadata; missing or mismatched content must fail closed.
- Verify authorization with cross-tenant negative tests; protect credentials and children’s data.
- No network dependency for previously downloaded lessons, recitation or local records.
- Never store secrets or signing keys in Git.
- Update README and test evidence with each implemented milestone. A successful GitHub commit alone is not an APK or device certification.
