package com.zamcan.madrassa.domain.salah;

/**
 * Produces a neutral local reminder prompt.
 *
 * <p>The app does not synthesize or claim to provide an authentic adhān
 * recitation. A future build may play a separately licensed/verified
 * recording; until then this class intentionally speaks only a factual
 * prayer-time reminder.</p>
 */
public final class AdhanVoiceEngine {

    private AdhanVoiceEngine() {
    }

    public static String reminderText(
            String language,
            PrayerTime.Prayer prayer
    ) {
        return reminderText(
                language,
                prayer,
                prayer == null ? "" : prayer.name()
        );
    }

    public static String reminderText(
            String language,
            PrayerTime.Prayer prayer,
            String prayerName
    ) {
        if (prayer == null) {
            return "";
        }

        String name = prayerName == null || prayerName.trim().isEmpty()
                ? prayer.name()
                : prayerName;
        if ("ar".equals(language)) {
            return "حان الآن وقت صلاة " + name
                    + " وفق الحساب المحلي.";
        }
        if ("en".equals(language)) {
            return "It is now the calculated time for "
                    + name
                    + " prayer.";
        }
        return "Ndio saa ya "
                + name
                + " kwa kielelezo cha eneo lako.";
    }
}
