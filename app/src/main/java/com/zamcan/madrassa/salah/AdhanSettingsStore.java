package com.zamcan.madrassa.salah;

import android.content.Context;
import android.content.SharedPreferences;

import com.zamcan.madrassa.domain.salah.AdhanSettings;

/** Android-local preference adapter for the optional device Adhan setting. */
public final class AdhanSettingsStore {
    private static final String PREFS = "edunoor_salah";
    private static final String ENABLED = "adhan_enabled";
    private AdhanSettingsStore() {}

    public static AdhanSettings read(Context context) {
        boolean enabled = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(ENABLED, false);
        AdhanSettings defaults = AdhanSettings.defaults();
        return new AdhanSettings(enabled, defaults.useVoice, defaults.useFajrSpecial, defaults.iqamaMinutes);
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(ENABLED, enabled).apply();
    }
}
