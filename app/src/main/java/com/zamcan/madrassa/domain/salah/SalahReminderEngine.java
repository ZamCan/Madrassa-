package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Domain facade for today's calculated prayer schedule. */
public final class SalahReminderEngine {

    private SalahReminderEngine() {
    }

    public static List<PrayerTime> today(
            LocalDate date,
            LocationProfile location
    ) {
        return PrayerTimesCalculator.calculate(date, location);
    }

    public static List<PrayerTime> today(
            LocalDate date,
            LocationProfile location,
            PrayerCalculationSettings settings
    ) {
        return PrayerTimesCalculator.calculate(date, location, settings);
    }

    public static PrayerTime.Prayer dueNow(
            LocalDateTime now,
            LocationProfile location,
            PrayerTime.Prayer alreadyTriggered
    ) {
        return dueNow(
                now,
                location,
                PrayerCalculationSettings.defaultSettings(),
                alreadyTriggered
        );
    }

    public static PrayerTime.Prayer dueNow(
            LocalDateTime now,
            LocationProfile location,
            PrayerCalculationSettings settings,
            PrayerTime.Prayer alreadyTriggered
    ) {
        if (now == null || location == null || settings == null) {
            return null;
        }

        return AdhanTriggerPolicy.duePrayer(
                now,
                today(now.toLocalDate(), location, settings),
                alreadyTriggered
        );
    }

    /** Never plays before the calculated time; allows a short late window. */
    public static boolean mayPlayAdhan(
            LocalDateTime now,
            PrayerTime scheduled
    ) {
        if (now == null
                || scheduled == null
                || !scheduled.available
                || scheduled.prayer == PrayerTime.Prayer.SUNRISE) {
            return false;
        }

        LocalDateTime due = LocalDateTime.of(
                now.toLocalDate(),
                scheduled.time
        );

        return !now.isBefore(due)
                && now.isBefore(due.plusMinutes(5));
    }
}
