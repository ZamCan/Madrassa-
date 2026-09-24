package com.zamcan.madrassa.salah;

import android.content.Context;
import android.content.SharedPreferences;

import com.zamcan.madrassa.domain.geography.LocationProfile;

/** Persists the user's last selected calculation location locally. */
public final class LocationProfileStore {

    private static final String PREFS = "edunoor_prayer_location";
    private static final String COUNTRY = "country";
    private static final String CITY = "city";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String OFFSET = "offset";

    private final SharedPreferences preferences;

    public LocationProfileStore(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public LocationProfile load() {
        if (!preferences.contains(LATITUDE)
                || !preferences.contains(LONGITUDE)
                || !preferences.contains(OFFSET)) {
            return null;
        }

        try {
            return new LocationProfile(
                    preferences.getString(COUNTRY, ""),
                    preferences.getString(CITY, ""),
                    preferences.getFloat(LATITUDE, Float.NaN),
                    preferences.getFloat(LONGITUDE, Float.NaN),
                    preferences.getFloat(OFFSET, Float.NaN)
            );
        } catch (IllegalArgumentException invalid) {
            preferences.edit().clear().apply();
            return null;
        }
    }

    public void save(LocationProfile location) {
        if (location == null) {
            throw new IllegalArgumentException("location is required");
        }

        preferences.edit()
                .putString(COUNTRY, location.countryCode)
                .putString(CITY, location.city)
                .putFloat(LATITUDE, (float) location.latitude)
                .putFloat(LONGITUDE, (float) location.longitude)
                .putFloat(OFFSET, (float) location.utcOffsetHours)
                .apply();
    }
}
