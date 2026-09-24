package com.zamcan.madrassa.domain.salah;

/** User-configurable, deterministic prayer calculation parameters. */
public final class PrayerCalculationSettings {

    public enum Method {
        MUSLIM_WORLD_LEAGUE(18.0, 17.0),
        ISNA(15.0, 15.0),
        EGYPT(19.5, 17.5),
        KARACHI(18.0, 18.0);

        final double fajrAngle;
        final double ishaAngle;

        Method(double fajrAngle, double ishaAngle) {
            this.fajrAngle = fajrAngle;
            this.ishaAngle = ishaAngle;
        }
    }

    public enum AsrMadhhab {
        STANDARD(1.0),
        HANAFI(2.0);

        final double shadow;

        AsrMadhhab(double shadow) {
            this.shadow = shadow;
        }
    }

    public final Method method;
    public final AsrMadhhab asrMadhhab;
    public final boolean highLatitudeAdjustment;

    public PrayerCalculationSettings(
            Method method,
            AsrMadhhab asrMadhhab,
            boolean highLatitudeAdjustment
    ) {
        if (method == null || asrMadhhab == null) {
            throw new IllegalArgumentException(
                    "calculation method and madhhab are required"
            );
        }
        this.method = method;
        this.asrMadhhab = asrMadhhab;
        this.highLatitudeAdjustment = highLatitudeAdjustment;
    }

    public static PrayerCalculationSettings defaultSettings() {
        return new PrayerCalculationSettings(
                Method.MUSLIM_WORLD_LEAGUE,
                AsrMadhhab.STANDARD,
                true
        );
    }
}
