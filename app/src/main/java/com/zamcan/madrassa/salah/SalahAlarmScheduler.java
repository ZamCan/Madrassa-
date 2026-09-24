package com.zamcan.madrassa.salah;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Schedules one deterministic alarm per prayer for a day.
 *
 * <p>Sunrise is excluded. Re-scheduling a day first cancels that day's
 * request codes, so enabling/disabling or changing location cannot create
 * duplicate alarms. Exact scheduling is used when the platform grants it;
 * otherwise the scheduler falls back to an idle-safe inexact alarm and
 * returns false so the UI can disclose the limitation.</p>
 */
public final class SalahAlarmScheduler {

    private static final String ACTION = "com.zamcan.madrassa.SALAH_ALARM";

    private SalahAlarmScheduler() {
    }

    public static boolean scheduleDay(
            Context context,
            LocalDate date,
            LocationProfile location
    ) {
        if (context == null || date == null || location == null) {
            throw new IllegalArgumentException(
                    "context/date/location are required"
            );
        }

        AlarmManager alarms = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (alarms == null) {
            return false;
        }

        cancelDay(context, date);

        List<PrayerTime> schedule =
                PrayerTimesCalculator.calculate(date, location);

        int offsetSeconds = (int) Math.round(
                location.utcOffsetHours * 3600.0
        );
        boolean exact = true;
        boolean scheduledAny = false;

        for (PrayerTime prayerTime : schedule) {
            if (prayerTime == null
                    || !prayerTime.available
                    || prayerTime.prayer == PrayerTime.Prayer.SUNRISE) {
                continue;
            }

            long when = date.atTime(prayerTime.time)
                    .atZone(ZoneOffset.ofTotalSeconds(offsetSeconds))
                    .toInstant()
                    .toEpochMilli();

            if (when <= System.currentTimeMillis()) {
                continue;
            }

            Intent intent = new Intent(context, AdhanAlarmReceiver.class)
                    .setAction(ACTION)
                    .putExtra(AdhanAlarmReceiver.EXTRA_PRAYER,
                            prayerTime.prayer.name())
                    .putExtra(AdhanAlarmReceiver.EXTRA_DATE,
                            date.toString())
                    .putExtra(AdhanAlarmReceiver.EXTRA_COUNTRY,
                            location.countryCode)
                    .putExtra(AdhanAlarmReceiver.EXTRA_CITY,
                            location.city)
                    .putExtra(AdhanAlarmReceiver.EXTRA_LATITUDE,
                            location.latitude)
                    .putExtra(AdhanAlarmReceiver.EXTRA_LONGITUDE,
                            location.longitude)
                    .putExtra(AdhanAlarmReceiver.EXTRA_OFFSET,
                            location.utcOffsetHours);

            PendingIntent pendingIntent = pendingIntent(
                    context,
                    requestCode(date, prayerTime.prayer),
                    intent
            );

            try {
                if (Build.VERSION.SDK_INT >= 31
                        && !alarms.canScheduleExactAlarms()) {
                    alarms.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            when,
                            pendingIntent
                    );
                    exact = false;
                } else {
                    alarms.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            when,
                            pendingIntent
                    );
                }
                scheduledAny = true;
            } catch (SecurityException denied) {
                try {
                    alarms.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            when,
                            pendingIntent
                    );
                    exact = false;
                    scheduledAny = true;
                } catch (RuntimeException unavailable) {
                    // A platform without any alarm capability is a
                    // graceful reminder limitation, not an app crash.
                }
            } catch (RuntimeException unavailable) {
                // Keep scheduling the remaining prayers; report inexact
                // scheduling through the boolean result.
                exact = false;
            }
        }

        return scheduledAny && exact;
    }

    public static void cancelDay(
            Context context,
            LocalDate date
    ) {
        if (context == null || date == null) {
            return;
        }

        AlarmManager alarms = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (alarms == null) {
            return;
        }

        for (PrayerTime.Prayer prayer : PrayerTime.Prayer.values()) {
            if (prayer == PrayerTime.Prayer.SUNRISE) {
                continue;
            }

            Intent intent = new Intent(context, AdhanAlarmReceiver.class)
                    .setAction(ACTION)
                    .putExtra(AdhanAlarmReceiver.EXTRA_DATE,
                            date.toString())
                    .putExtra(AdhanAlarmReceiver.EXTRA_PRAYER,
                            prayer.name());

            PendingIntent pendingIntent = pendingIntent(
                    context,
                    requestCode(date, prayer),
                    intent
            );
            alarms.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static PendingIntent pendingIntent(
            Context context,
            int requestCode,
            Intent intent
    ) {
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static int requestCode(
            LocalDate date,
            PrayerTime.Prayer prayer
    ) {
        return 31 * (int) (date.toEpochDay() & 0x0000ffff)
                + prayer.ordinal();
    }
}
