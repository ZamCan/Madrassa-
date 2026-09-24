# Madrassa — EduNoor

EduNoor is a calm, local-first Madrassa and parent portal built with
Android Java and platform SQLite. The current source is a working
foundation, not a claim that every planned Madrassa workflow is already
production-complete.

## Current local-first foundation

- **Solo Learning** — bundled, trilingual Wudu/Salah/Qur'an learning
  material with offline lesson audio and local progress UI.
- **Parent** — PBKDF2-backed local authentication, parent/child
  relationship checks, and a read-only view of authorized children and
  their stored fees/progress.
- **Ustadh** — local Madrassa authentication, tenant-scoped read-only
  operations views, academic programme setup, and explicit access
  context for domain writes.
- **Academic Core** — Course, AcademicUnit, Lesson, LearningMaterial,
  Assignment, Assessment, LearningProgress, Programme, enrollment,
  classes/groups, and relationship validation behind repository
  interfaces.
- **Islamic tools** — offline Qibla bearing, configurable prayer-time
  calculations, neutral local prayer reminders, Hijri/Gregorian
  calendar, and an optional home-screen calendar widget.
- **Localization** — Swahili (default), English, and Arabic with RTL
  layout support.
- **Data safety** — local SQLite migrations, disabled platform backup,
  parameterized queries, and domain-level tenant policies.

## Explicitly not claimed

- No Firebase, Supabase, cloud API, or invented cloud identity.
- No full Qur'an corpus is bundled; the current Solo library contains
  the checked-in educational lessons only.
- Bundled lesson audio is not independently certified as authentic
  Qur'an recitation.
- The current reminder is a neutral calculated-time prompt, not a
  synthesized or bundled authentic adhān recording.
- Password reset/OTP recovery, payment processing, server SMS, push
  notifications, and broad CRUD management remain future work unless
  the repository contains a real implementation.
- Parent and Ustadh dashboards intentionally present read-only local
  records; they do not show fake write success.

## Product principles

- Premium professional UX with Islamic identity and scholarly calm.
- Local-first data handling and no hidden cloud dependency.
- Secure parent access and explicit tenant authorization.
- Honest incomplete states instead of simulated production behavior.
- Preserve database data and migration history.
- Verify an installable APK on a second physical Android device before
  calling a stage device-verified.

## Development rule

Every major stage must produce an installable APK and be tested on a
second Android device before the next major stage is called complete.
