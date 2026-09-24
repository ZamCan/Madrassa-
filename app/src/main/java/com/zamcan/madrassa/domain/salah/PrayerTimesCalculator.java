package com.zamcan.madrassa.domain.salah;

import com.zamcan.madrassa.domain.geography.LocationProfile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Offline solar prayer-time calculation.
 *
 * <p>This is a planning aid, not a religious authority. The selected
 * method, Asr convention, location and UTC offset are explicit inputs;
 * callers should show that context to the user. Sunrise is returned for
 * display but is never treated as an Adhan prayer.</p>
 */
public final class PrayerTimesCalculator {

    private static final double PI = Math.PI;

    private PrayerTimesCalculator() {
    }

    public static List<PrayerTime> calculate(
            LocalDate date,
            LocationProfile location
    ) {
        return calculate(
                date,
                location,
                PrayerCalculationSettings.defaultSettings()
        );
    }

    public static List<PrayerTime> calculate(
            LocalDate date,
            LocationProfile location,
            PrayerCalculationSettings settings
    ) {
        if (date == null || location == null || settings == null) {
            throw new IllegalArgumentException(
                    "date/location/settings are required"
            );
        }

        int dayOfYear = date.getDayOfYear();
        double g = 2.0 * PI / 365.0 * (dayOfYear - 1.0);

        double equationOfTime = 229.18 * (
                0.000075
                        + 0.001868 * Math.cos(g)
                        - 0.032077 * Math.sin(g)
                        - 0.014615 * Math.cos(2.0 * g)
                        - 0.040849 * Math.sin(2.0 * g)
        );

        double declination = 0.006918
                - 0.399912 * Math.cos(g)
                + 0.070257 * Math.sin(g)
                - 0.006758 * Math.cos(2.0 * g)
                + 0.000907 * Math.sin(2.0 * g)
                - 0.002697 * Math.cos(3.0 * g)
                + 0.001480 * Math.sin(3.0 * g);

        double noon = 720.0
                - 4.0 * location.longitude
                - equationOfTime
                + location.utcOffsetHours * 60.0;

        Event sunrise = event(
                noon,
                location.latitude,
                declination,
                -0.833,
                false,
                settings.highLatitudeAdjustment
        );
        Event sunset = event(
                noon,
                location.latitude,
                declination,
                -0.833,
                true,
                settings.highLatitudeAdjustment
        );
        Event fajr = event(
                noon,
                location.latitude,
                declination,
                -settings.method.fajrAngle,
                false,
                settings.highLatitudeAdjustment
        );
        Event isha = event(
                noon,
                location.latitude,
                declination,
                -settings.method.ishaAngle,
                true,
                settings.highLatitudeAdjustment
        );
        Event asr = asr(
                noon,
                location.latitude,
                declination,
                settings.asrMadhhab.shadow,
                settings.highLatitudeAdjustment
        );

        List<PrayerTime> result = new ArrayList<>();
        result.add(new PrayerTime(
                PrayerTime.Prayer.FAJR,
                toTime(fajr.minutes),
                fajr.available
        ));
        result.add(new PrayerTime(
                PrayerTime.Prayer.SUNRISE,
                toTime(sunrise.minutes),
                sunrise.available
        ));
        result.add(new PrayerTime(
                PrayerTime.Prayer.DHUHR,
                toTime(noon),
                true
        ));
        result.add(new PrayerTime(
                PrayerTime.Prayer.ASR,
                toTime(asr.minutes),
                asr.available
        ));
        result.add(new PrayerTime(
                PrayerTime.Prayer.MAGHRIB,
                toTime(sunset.minutes),
                sunset.available
        ));
        result.add(new PrayerTime(
                PrayerTime.Prayer.ISHA,
                toTime(isha.minutes),
                isha.available
        ));

        return Collections.unmodifiableList(result);
    }

    private static Event event(
            double noon,
            double latitude,
            double declination,
            double altitude,
            boolean evening,
            boolean highLatitudeAdjustment
    ) {
        double phi = Math.toRadians(latitude);
        double zenith = Math.toRadians(90.0 - altitude);
        double denominator = Math.cos(phi) * Math.cos(declination);
        double cosH = denominator == 0.0
                ? 2.0
                : (Math.cos(zenith)
                - Math.sin(phi) * Math.sin(declination)) / denominator;

        if (cosH > 1.0 || cosH < -1.0) {
            if (!highLatitudeAdjustment) {
                return new Event(noon, false);
            }

            // Polar/high-latitude fallback: keep a safe, deterministic
            // estimate and mark it so the UI can disclose approximation.
            double offset = evening ? 120.0 : -120.0;
            return new Event(noon + offset, false);
        }

        double hourAngle = Math.toDegrees(Math.acos(cosH));
        return new Event(
                noon + (evening ? 4.0 * hourAngle : -4.0 * hourAngle),
                true
        );
    }

    private static Event asr(
            double noon,
            double latitude,
            double declination,
            double shadowFactor,
            boolean highLatitudeAdjustment
    ) {
        double phi = Math.toRadians(latitude);
        double altitude = Math.toDegrees(Math.atan(
                1.0 / (shadowFactor + Math.tan(Math.abs(phi - declination)))
        ));
        return event(
                noon,
                latitude,
                declination,
                altitude,
                true,
                highLatitudeAdjustment
        );
    }

    private static LocalTime toTime(double minutes) {
        long rounded = Math.round(minutes);
        long normalized = ((rounded % 1440L) + 1440L) % 1440L;
        return LocalTime.of(
                (int) (normalized / 60L),
                (int) (normalized % 60L)
        );
    }

    private static final class Event {
        final double minutes;
        final boolean available;

        Event(double minutes, boolean available) {
            this.minutes = minutes;
            this.available = available;
        }
    }
}
