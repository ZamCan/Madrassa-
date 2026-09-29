# EduNoor — Solo Learning & Islamic Surface Master Prompt

Build only on the existing local-first architecture. Do not add Firebase, Supabase, cloud APIs, provider SDKs, destructive migrations, fake content or fake network success.

## Solo Learning
Create a premium calm learning surface for Qur'an, Salah, Hadith, Tawheed, Fiqh, Barazanj, Adab and books. Solo is a local learner context and must not depend on Parent/Ustadh/Madrassa authentication.

Navigation:
Home → subject → book/lesson → reading/listening/review → progress.

Use canonical global content references. Do not duplicate Madrassa academic records. Keep progress local and deterministic.

## Qur'an
Support canonical 114-surah / 30-juz navigation, surah selection, ayah-level study references, reading, revision and Hifz review. Do not invent Qur'an text. Text/audio datasets must be authoritative licensed assets.

## Salah
Use device/location only with explicit permission. Calculate local prayer times offline. Provide method, madhhab and high-latitude settings. Qibla uses the Kaaba great-circle bearing. Adhan is OFF by default and must never play before the calculated prayer time. Iqama is a configurable local reminder, not an automatic claim that a mosque's iqama time is known.

## Islamic calendar
Show dual Gregorian/Hijri dates. Android home/lock-screen widget is optional and device-dependent; do not claim that an app can force a widget onto a user's lock screen. Preserve the system calendar.

## Knowledge and books
Use stable subject/resource taxonomies: Qur'an, Tajwid, Hifz, Tafsir, Hadith, Tawheed/Aqidah, Fiqh, Seerah, Barazanj, Adab, Arabic, Salah, Dua, Islamic Calendar, General Knowledge. Resource types: lesson, book, text, document, audio, image, Qur'an reference, Hadith reference.

## Visuals
Use the existing parchment/walnut/clay/gold design system. Prefer vector drawables, typography, geometric motifs and licensed/local assets. Never add noisy emoji, decorative sound effects or generic AI imagery. A Kaaba/Qibla surface may use a professional vector/PNG asset later; keep the UI functional without it.

## Gateway readiness
Payment, SMS, server sync and notification gateways remain provider-neutral interfaces. Their domain logic can be prepared internally, but no external provider integration is required in this stage.

## Verification
Inspect actual repository state; preserve unrelated work; run git diff --check; compile when the Android SDK is available; never claim success without evidence.
