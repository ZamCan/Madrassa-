package com.zamcan.madrassa.salah;

import android.content.Context;
import android.content.SharedPreferences;

import com.zamcan.madrassa.domain.salah.AdhanSettings;
import com.zamcan.madrassa.domain.salah.PrayerCalculationSettings;

/** Small local preference boundary; no account or cloud identity. */
public final class AdhanSettingsStore {

    private static final String PREFS = "edunoor_salah_settings";
    private static final String ENABLED = "adhan_enabled";
    private static final String SPOKEN = "adhan_spoken";

    private final SharedPreferences preferences;

    public AdhanSettingsStore(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public AdhanSettings load() {
        return new AdhanSettings(
                preferences.getBoolean(ENABLED, false),
                preferences.getBoolean(SPOKEN, false),
                PrayerCalculationSettings.defaultSettings()
        );
    }

    public void save(AdhanSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings is required");
        }

        preferences.edit()
                .putBoolean(ENABLED, settings.enabled)
                .putBoolean(SPOKEN, settings.spokenReminder)
                .apply();
    }
}
