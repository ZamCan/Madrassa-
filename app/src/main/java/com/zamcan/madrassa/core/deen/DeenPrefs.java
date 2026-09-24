package com.zamcan.madrassa.core.deen;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

/**
 * Deen services preferences (salat location, adhana, sound,
 * Hijri display adjustment). One small store shared by the Deen
 * panel, the calendar surfaces and the sound engine. On-device
 * only, nothing syncs.
 */
public final class DeenPrefs {

    private static final String STORE = "deen_prefs";

    /** Dar es Salaam default (the app's home city). */
    public static final double DEFAULT_LAT = -6.7924;
    public static final double DEFAULT_LON = 39.2083;
    public static final double DEFAULT_TZ = 3.0;

    private DeenPrefs() {
    }

    private static SharedPreferences store(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(STORE, Context.MODE_PRIVATE);
    }

    public static boolean isSoundOn(Context context) {
        return store(context).getBoolean("sound_on", true);
    }

    public static void setSoundOn(Context context, boolean on) {
        store(context).edit().putBoolean("sound_on", on).apply();
    }

    public static boolean isAdhanOn(Context context) {
        return store(context).getBoolean("adhan_on", true);
    }

    public static void setAdhanOn(Context context, boolean on) {
        store(context).edit().putBoolean("adhan_on", on).apply();
    }

    public static double latitude(Context context) {
        return Double.longBitsToDouble(
                store(context).getLong("lat",
                        Double.doubleToRawLongBits(DEFAULT_LAT)));
    }

    public static double longitude(Context context) {
        return Double.longBitsToDouble(
                store(context).getLong("lon",
                        Double.doubleToRawLongBits(DEFAULT_LON)));
    }

    public static double timeZoneOffset(Context context) {
        return store(context).getFloat("tz", (float) DEFAULT_TZ);
    }

    public static void setLocation(
            Context context, double lat, double lon) {
        store(context).edit()
                .putLong("lat", Double.doubleToRawLongBits(lat))
                .putLong("lon", Double.doubleToRawLongBits(lon))
                .apply();
    }

    /** Hijri display adjustment in days (-2..+2, default 0). */
    public static int hijriAdjust(Context context) {
        return store(context).getInt("hijri_adjust", 0);
    }

    public static void setHijriAdjust(Context context, int days) {
        store(context).edit()
                .putInt("hijri_adjust",
                        Math.max(-2, Math.min(2, days)))
                .apply();
    }

    /** Today shifted by the user's Hijri display adjustment. */
    public static LocalDate adjustedToday(Context context) {
        return LocalDate.now().plusDays(hijriAdjust(context));
    }
}
