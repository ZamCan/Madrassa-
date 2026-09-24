package com.zamcan.madrassa.salah;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public final class SalahAlarmScheduler {
    private SalahAlarmScheduler() {}

    public static void scheduleDay(
            Context context,
            LocalDate date,
            LocationProfile location
    ) {
        if (context == null || date == null || location == null) {
            throw new IllegalArgumentException("context/date/location required");
        }

        AlarmManager alarms =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;

        List<PrayerTime> schedule =
                PrayerTimesCalculator.calculate(date, location);

        for (PrayerTime p : schedule) {
            if (p.prayer == PrayerTime.Prayer.SUNRISE) continue;

            int offsetMinutes =
                    (int) Math.round(location.utcOffsetHours * 60.0);

            long when = date.atTime(p.time)
                    .toInstant(ZoneOffset.ofTotalSeconds(offsetMinutes * 60))
                    .toEpochMilli();

            if (when <= System.currentTimeMillis()) continue;

            Intent intent = new Intent(context, AdhanAlarmReceiver.class)
                    .putExtra("prayer", p.prayer.name())
                    .putExtra("country", location.countryCode)
                    .putExtra("city", location.city)
                    .putExtra("lat", location.latitude)
                    .putExtra("lon", location.longitude)
                    .putExtra("offset", location.utcOffsetHours);

            int requestCode =
                    (int) (Math.abs(date.toEpochDay()) * 10
                            + (p.prayer.ordinal() + 1));

            PendingIntent pending =
                    PendingIntent.getBroadcast(
                            context,
                            requestCode,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            if (android.os.Build.VERSION.SDK_INT >= 31
                    && !alarms.canScheduleExactAlarms()) {
                alarms.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        when,
                        pending
                );
            } else {
                alarms.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        when,
                        pending
                );
            }
        }
    }

    public static void cancelDay(
            Context context,
            LocalDate date,
            LocationProfile location
    ) {
        if (context == null || date == null || location == null) return;

        AlarmManager alarms =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;

        List<PrayerTime> schedule =
                PrayerTimesCalculator.calculate(date, location);

        for (PrayerTime p : schedule) {
            if (p.prayer == PrayerTime.Prayer.SUNRISE) continue;

            int requestCode =
                    (int) (Math.abs(date.toEpochDay()) * 10
                            + (p.prayer.ordinal() + 1));

            Intent intent =
                    new Intent(context, AdhanAlarmReceiver.class)
                            .putExtra("prayer", p.prayer.name());

            PendingIntent pending =
                    PendingIntent.getBroadcast(
                            context,
                            requestCode,
                            intent,
                            PendingIntent.FLAG_NO_CREATE
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            if (pending != null) {
                alarms.cancel(pending);
                pending.cancel();
            }
        }
    }
}
