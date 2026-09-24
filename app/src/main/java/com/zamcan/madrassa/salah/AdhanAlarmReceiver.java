package com.zamcan.madrassa.salah;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.domain.geography.LocationCatalog;
import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.AdhanVoiceEngine;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.PrayerTimesCalculator;
import com.zamcan.madrassa.domain.salah.SalahReminderEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/**
 * Receives a local alarm and speaks a neutral calculated-time reminder.
 *
 * <p>No adhān recording is bundled or synthesized. The receiver is
 * deliberately defensive about stale alarms, invalid coordinates and
 * permission/security failures.</p>
 */
public final class AdhanAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_PRAYER = "prayer";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_COUNTRY = "country";
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_OFFSET = "offset";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAfterBoot(context);
            return;
        }

        if (!new AdhanSettingsStore(context).load().enabled) {
            return;
        }

        String prayerName = intent.getStringExtra(EXTRA_PRAYER);
        String dateValue = intent.getStringExtra(EXTRA_DATE);

        if (prayerName == null || dateValue == null) {
            return;
        }

        PrayerTime.Prayer prayer;
        LocalDate date;
        try {
            prayer = PrayerTime.Prayer.valueOf(prayerName);
            date = LocalDate.parse(dateValue);
        } catch (RuntimeException invalid) {
            return;
        }

        if (prayer == PrayerTime.Prayer.SUNRISE) {
            return;
        }

        try {
            LocationProfile location = new LocationProfile(
                    intent.getStringExtra(EXTRA_COUNTRY),
                    intent.getStringExtra(EXTRA_CITY),
                    intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN),
                    intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN),
                    intent.getDoubleExtra(EXTRA_OFFSET, Double.NaN)
            );
            int offsetSeconds = (int) Math.round(
                    location.utcOffsetHours * 3600.0
            );
            ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSeconds);

            if (!LocalDate.now(offset).equals(date)) {
                return;
            }

            List<PrayerTime> schedule =
                    PrayerTimesCalculator.calculate(date, location);
            PrayerTime target = null;
            for (PrayerTime value : schedule) {
                if (value.prayer == prayer) {
                    target = value;
                    break;
                }
            }

            if (target == null
                    || !SalahReminderEngine.mayPlayAdhan(
                    LocalDateTime.now(offset),
                    target
            )) {
                return;
            }

            speakReminder(context, prayer);
        } catch (RuntimeException ignored) {
            // A bad/stale location must never crash the receiver.
        }
    }

    private void rescheduleAfterBoot(Context context) {
        try {
            if (!new AdhanSettingsStore(context).load().enabled) {
                return;
            }

            LocationProfile location = new LocationProfileStore(context)
                    .load();
            if (location == null) {
                location = LocationCatalog.defaultProfile();
            }

            ZoneOffset offset = ZoneOffset.ofTotalSeconds((int) Math.round(
                    location.utcOffsetHours * 3600.0
            ));
            for (int i = 0; i < 7; i++) {
                SalahAlarmScheduler.scheduleDay(
                        context,
                        LocalDate.now(offset).plusDays(i),
                        location
                );
            }
        } catch (RuntimeException ignored) {
            // Boot scheduling is best effort; the app remains usable when
            // the platform denies alarms or the saved location is invalid.
        }
    }

    private void speakReminder(
            Context context,
            PrayerTime.Prayer prayer
    ) {
        PendingResult pendingResult = goAsync();
        final Context appContext = context.getApplicationContext();
        final String language = LanguageManager.getLanguage(appContext);
        Context localizedContext = LanguageManager.wrap(appContext);
        String[] prayerNames = localizedContext.getResources().getStringArray(
                com.zamcan.madrassa.R.array.prayer_names
        );
        String prayerName = prayer.ordinal() < prayerNames.length
                ? prayerNames[prayer.ordinal()]
                : prayer.name();
        final String text = AdhanVoiceEngine.reminderText(
                language,
                prayer,
                prayerName
        );
        final TextToSpeech[] holder = new TextToSpeech[1];

        holder[0] = new TextToSpeech(appContext, status -> {
            TextToSpeech tts = holder[0];
            if (tts == null) {
                pendingResult.finish();
                return;
            }
            if (status == TextToSpeech.SUCCESS && !text.isEmpty()) {
                Locale locale = "ar".equals(language)
                        ? new Locale("ar")
                        : Locale.ENGLISH;
                int languageStatus = tts.setLanguage(locale);
                if (languageStatus != TextToSpeech.LANG_MISSING_DATA
                        && languageStatus != TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.speak(
                            text,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "edunoor_prayer_reminder"
                    );
                }
            }

            // TTS engines do not expose a single reliable completion
            // callback across all supported Android versions. A bounded
            // cleanup window prevents a receiver-held process leak.
            new android.os.Handler(LooperHolder.mainLooper()).postDelayed(
                    () -> {
                        try {
                            tts.stop();
                            tts.shutdown();
                        } catch (RuntimeException ignored) {
                        }
                        pendingResult.finish();
                    },
                    8_000L
            );
        });
    }

    private static final class LooperHolder {
        static android.os.Looper mainLooper() {
            return android.os.Looper.getMainLooper();
        }
    }
}
