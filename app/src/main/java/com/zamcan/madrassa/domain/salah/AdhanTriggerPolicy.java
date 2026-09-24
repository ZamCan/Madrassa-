package com.zamcan.madrassa.domain.salah;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Exact-minute and delayed-delivery policy for local prayer reminders. */
public final class AdhanTriggerPolicy {

    private AdhanTriggerPolicy() {
    }

    /**
     * Returns a due prayer only at its scheduled minute. Sunrise is never
     * returned, and a caller-provided already-triggered prayer is skipped.
     */
    public static PrayerTime.Prayer duePrayer(
            LocalDateTime now,
            List<PrayerTime> schedule,
            PrayerTime.Prayer alreadyTriggered
    ) {
        if (now == null || schedule == null) {
            return null;
        }

        LocalTime current = now.toLocalTime()
                .withSecond(0)
                .withNano(0);

        for (PrayerTime prayer : schedule) {
            if (prayer == null
                    || !prayer.available
                    || prayer.prayer == PrayerTime.Prayer.SUNRISE) {
                continue;
            }

            if (prayer.time.equals(current)
                    && prayer.prayer != alreadyTriggered) {
                return prayer.prayer;
            }
        }

        return null;
    }

    public static boolean isIqamaWindow(
            LocalDateTime now,
            LocalTime adhanTime,
            int minutesAfter
    ) {
        if (now == null
                || adhanTime == null
                || minutesAfter < 0) {
            return false;
        }

        LocalTime current = now.toLocalTime();
        LocalTime end = adhanTime.plusMinutes(minutesAfter);
        return !current.isBefore(adhanTime) && current.isBefore(end);
    }
}
