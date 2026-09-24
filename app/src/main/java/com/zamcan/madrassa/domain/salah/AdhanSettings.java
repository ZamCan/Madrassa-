package com.zamcan.madrassa.domain.salah;

/** Local reminder settings. No cloud or server state is involved. */
public final class AdhanSettings {

    public final boolean enabled;
    public final boolean spokenReminder;
    public final PrayerCalculationSettings calculationSettings;

    public AdhanSettings(
            boolean enabled,
            boolean spokenReminder,
            PrayerCalculationSettings calculationSettings
    ) {
        if (calculationSettings == null) {
            throw new IllegalArgumentException(
                    "calculationSettings is required"
            );
        }
        this.enabled = enabled;
        this.spokenReminder = spokenReminder;
        this.calculationSettings = calculationSettings;
    }

    public static AdhanSettings defaults() {
        return new AdhanSettings(
                false,
                false,
                PrayerCalculationSettings.defaultSettings()
        );
    }
}
