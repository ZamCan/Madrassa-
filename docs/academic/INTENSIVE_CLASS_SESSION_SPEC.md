# EduNoor Intensive Academic Learning Core

## Purpose
The academic surface is a real Madrassa teaching system. It represents knowledge, teaching sequence, learner activity, evidence of learning and revision rather than merely displaying subject cards.

## Class-session contract
Every lesson should be executable through this sequence:
1. Opening / intention / attendance
2. Revision of prerequisite knowledge
3. Learning objectives
4. Teacher explanation
5. Teacher demonstration
6. Guided learner practice
7. Individual learner turns
8. Correction and feedback
9. Evidence check / assessment
10. Homework or revision assignment
11. Mastery decision
12. Progress record

## Knowledge object
Each lesson should contain: stable lesson id; subject id; level; title in Swahili, English and Arabic; duration; objectives; teacher flow; core knowledge; worked examples where appropriate; learner practice; assessment prompts; homework/revision; source metadata; curriculum/madhhab scope where relevant.

## Content integrity
EduNoor must not manufacture Islamic source material.
- Quran references identify surah/ayah.
- Hadith entries identify collection and reference where available.
- Hadith grading is attributed to a reliable source when included.
- Fiqh differences are labelled by adopted curriculum/madhhab rather than presented as universal facts.
- Sirah material preserves source metadata and distinguishes well-attested material from reports requiring caution.
- Teacher-created material is distinguishable from primary religious sources.

## Native design
The class screen uses the existing EduNoor visual language rather than a web-style dashboard: calm parchment/canvas surfaces, walnut typography, clay/antique-gold accents, compact phase rail, readable knowledge blocks, explicit teacher actions, no decorative AI/chatbot treatment, and offline bundled curriculum data.

## Current foundation pack
app/src/main/assets/academic/foundation_curriculum.json currently provides a structured first content slice covering Qur'an, Tajwid, Fiqh and worship, Hadith, Aqidah/Tawhid, Prophetic Sirah, Arabic, and Akhlaq/Adab.
The first slice contains twelve executable lessons with objectives, teacher flow, multilingual knowledge, assessment, homework and source metadata.
This is intentionally a foundation pack, not a claim that the entire Madrassa curriculum is complete.

## Next content expansion
Future curriculum packs should add Qur'an reading and recitation, Tajwid progression, Hifz planning and revision, Fiqh progression, Hadith studies, Aqidah/Tawhid, Sirah/Tarikh, Arabic reading/vocabulary/Nahwu/Sharaf, Akhlaq/Adab, higher-level Ulum al-Qur'an, higher-level Ulum al-Hadith, Faraidh and other programme-specific subjects.

The data model should remain reusable so one lesson can render to Ustadh teaching mode, learner study mode, revision mode and parent progress summaries without duplicating the knowledge itself.