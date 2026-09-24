package com.zamcan.madrassa.domain.salah;

import java.time.LocalTime;
import java.util.Objects;

/** One calculated prayer event. Sunrise is informational, not an Adhan. */
public final class PrayerTime {

    public enum Prayer {
        FAJR,
        SUNRISE,
        DHUHR,
        ASR,
        MAGHRIB,
        ISHA
    }

    public final Prayer prayer;
    public final LocalTime time;
    public final boolean available;

    public PrayerTime(Prayer prayer, LocalTime time) {
        this(prayer, time, true);
    }

    public PrayerTime(
            Prayer prayer,
            LocalTime time,
            boolean available
    ) {
        this.prayer = Objects.requireNonNull(prayer, "prayer");
        this.time = Objects.requireNonNull(time, "time");
        this.available = available;
    }

    @Override
    public String toString() {
        return prayer + " " + time + (available ? "" : " (estimated)");
    }
}
