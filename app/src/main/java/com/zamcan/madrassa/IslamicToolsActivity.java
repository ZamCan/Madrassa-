package com.zamcan.madrassa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.domain.geography.LocationCatalog;
import com.zamcan.madrassa.domain.geography.LocationProfile;
import com.zamcan.madrassa.domain.salah.AdhanSettings;
import com.zamcan.madrassa.domain.salah.PrayerTime;
import com.zamcan.madrassa.domain.salah.QiblaCalculator;
import com.zamcan.madrassa.domain.salah.SalahReminderEngine;
import com.zamcan.madrassa.salah.AdhanSettingsStore;
import com.zamcan.madrassa.salah.DeviceLocationProfileProvider;
import com.zamcan.madrassa.salah.LocationProfileStore;
import com.zamcan.madrassa.salah.SalahAlarmScheduler;
import com.zamcan.madrassa.ui.components.EduNoorButton;
import com.zamcan.madrassa.ui.components.EduNoorCard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

/**
 * Offline Qibla, prayer timetable, local reminder and calendar hub.
 *
 * <p>Calculations are explicitly labelled as estimates and use the
 * selected location/method. Location permission is optional: denial keeps
 * the bundled fallback profile and never blocks the rest of the screen.</p>
 */
public final class IslamicToolsActivity extends Activity {

    private static final int REQUEST_LOCATION = 71;

    private LocationProfile location;
    private AdhanSettingsStore settingsStore;
    private LocationProfileStore locationStore;
    private AdhanSettings settings;
    private TextView locationValue;
    private TextView qiblaValue;
    private TextView prayerValue;
    private TextView adhanButton;
    private TextView methodValue;

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(
            CharSequence value,
            float size,
            int color,
            boolean bold
    ) {
        TextView view = new TextView(this);
        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.create(
                "sans",
                bold ? Typeface.BOLD : Typeface.NORMAL
        ));
        view.setIncludeFontPadding(true);
        return view;
    }

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        location = LocationCatalog.defaultProfile();
        settingsStore = new AdhanSettingsStore(this);
        locationStore = new LocationProfileStore(this);
        LocationProfile savedLocation = locationStore.load();
        if (savedLocation != null) {
            location = savedLocation;
        }
        settings = settingsStore.load();

        int background = getColor(R.color.edunoor_background);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.edunoor_landing_bg);
        root.setPadding(dp(16), dp(10), dp(16), dp(18));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setContentDescription(getString(R.string.islamic_tools_back));
        back.setOnClickListener(v -> finish());
        header.addView(back, new LinearLayout.LayoutParams(dp(44), dp(48)));

        TextView title = text(
                getString(R.string.islamic_tools_title),
                21,
                getColor(R.color.edunoor_walnut),
                true
        );
        header.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        root.addView(header, new LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView intro = text(
                getString(R.string.islamic_tools_intro),
                11,
                getColor(R.color.edunoor_muted),
                false
        );
        LinearLayout.LayoutParams introParams =
                new LinearLayout.LayoutParams(-1, -2);
        introParams.topMargin = dp(4);
        root.addView(intro, introParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(12), 0, 0);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        buildLocationCard(content);
        buildQiblaCard(content);
        buildPrayerCard(content);
        buildAdhanCard(content);
        buildCalendarCard(content);

        refreshCalculations();

        if (settings.enabled && !rescheduleReminders()) {
            Toast.makeText(
                    this,
                    getString(R.string.islamic_adhan_inexact),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void buildLocationCard(LinearLayout content) {
        LinearLayout card = card();
        card.addView(sectionTitle(
                getString(R.string.islamic_location_title),
                getString(R.string.islamic_location_subtitle)
        ));

        locationValue = text(
                "",
                12,
                getColor(R.color.edunoor_ink_soft),
                false
        );
        locationValue.setPadding(0, dp(6), 0, dp(8));
        card.addView(locationValue);

        TextView choose = EduNoorButton.secondary(
                this,
                getString(DeviceLocationProfileProvider.hasPermission(this)
                        ? R.string.islamic_location_use_device
                        : R.string.islamic_location_allow_device)
        );
        choose.setOnClickListener(v -> {
            if (DeviceLocationProfileProvider.hasPermission(this)) {
                applyDeviceLocation();
            } else {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        REQUEST_LOCATION
                );
            }
        });
        card.addView(choose, new LinearLayout.LayoutParams(-1, dp(48)));
        content.addView(card, cardParams());
    }

    private void buildQiblaCard(LinearLayout content) {
        LinearLayout card = card();
        card.addView(sectionTitle(
                getString(R.string.islamic_qibla_title),
                getString(R.string.islamic_qibla_subtitle)
        ));
        qiblaValue = text("", 18,
                getColor(R.color.edunoor_walnut), true);
        qiblaValue.setGravity(Gravity.CENTER);
        qiblaValue.setPadding(0, dp(10), 0, dp(4));
        card.addView(qiblaValue);
        content.addView(card, cardParams());
    }

    private void buildPrayerCard(LinearLayout content) {
        LinearLayout card = card();
        card.addView(sectionTitle(
                getString(R.string.islamic_salah_title),
                getString(R.string.islamic_salah_subtitle)
        ));
        methodValue = text(
                getString(R.string.islamic_method_default),
                10.5f,
                getColor(R.color.edunoor_muted),
                false
        );
        methodValue.setPadding(0, dp(5), 0, dp(6));
        card.addView(methodValue);
        prayerValue = text("", 12,
                getColor(R.color.edunoor_ink_soft), false);
        prayerValue.setPadding(0, dp(4), 0, dp(8));
        card.addView(prayerValue);
        content.addView(card, cardParams());
    }

    private void buildAdhanCard(LinearLayout content) {
        LinearLayout card = card();
        card.addView(sectionTitle(
                getString(R.string.islamic_adhan_title),
                getString(R.string.islamic_adhan_subtitle)
        ));

        TextView notice = text(
                getString(R.string.islamic_adhan_neutral_reminder),
                10.5f,
                getColor(R.color.edunoor_muted),
                false
        );
        notice.setPadding(0, dp(4), 0, dp(8));
        card.addView(notice);

        adhanButton = EduNoorButton.primary(
                this,
                getString(settings.enabled
                        ? R.string.islamic_adhan_disable
                        : R.string.islamic_adhan_enable)
        );
        adhanButton.setOnClickListener(v -> toggleAdhan());
        card.addView(adhanButton, new LinearLayout.LayoutParams(-1, dp(50)));
        content.addView(card, cardParams());
    }

    private void buildCalendarCard(LinearLayout content) {
        LinearLayout card = EduNoorCard.create(
                this,
                getString(R.string.islamic_calendar_eyebrow),
                getString(R.string.islamic_calendar_title),
                getString(R.string.islamic_calendar_subtitle),
                "›"
        );
        card.setOnClickListener(v -> startActivity(new Intent(
                this,
                com.zamcan.madrassa.core.calendar.CalendarActivity.class
        )));
        content.addView(card, cardParams());
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.edunoor_canvas);
        return card;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        return params;
    }

    private LinearLayout sectionTitle(String title, String subtitle) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(text(title, 16,
                getColor(R.color.edunoor_walnut), true));
        group.addView(text(subtitle, 10.5f,
                getColor(R.color.edunoor_muted), false));
        return group;
    }

    private void refreshCalculations() {
        if (locationValue != null) {
            locationValue.setText(getString(
                    R.string.islamic_location_value,
                    location.city,
                    location.latitude,
                    location.longitude
            ));
        }

        try {
            double bearing = QiblaCalculator.bearingDegrees(
                    location.latitude,
                    location.longitude
            );
            if (qiblaValue != null) {
                qiblaValue.setText(getString(
                        R.string.islamic_qibla_value,
                        bearing,
                        QiblaCalculator.cardinal(bearing)
                ));
            }

            List<PrayerTime> times = SalahReminderEngine.today(
                    LocalDate.now(),
                    location,
                    settings.calculationSettings
            );
            if (prayerValue != null) {
                prayerValue.setText(formatPrayerTimes(times));
            }
            if (methodValue != null) {
                methodValue.setText(getString(
                        R.string.islamic_method_value,
                        methodLabel(
                                settings.calculationSettings.method.name()
                        ),
                        asrLabel(
                                settings.calculationSettings.asrMadhhab.name()
                        )
                ));
            }
        } catch (RuntimeException error) {
            if (prayerValue != null) {
                prayerValue.setText(
                        getString(R.string.islamic_calculation_unavailable)
                );
            }
            if (qiblaValue != null) {
                qiblaValue.setText(
                        getString(R.string.islamic_not_available)
                );
            }
        }
    }

    private String methodLabel(String value) {
        if ("ISNA".equals(value)) {
            return getString(R.string.method_isna);
        }
        if ("EGYPT".equals(value)) {
            return getString(R.string.method_egypt);
        }
        if ("KARACHI".equals(value)) {
            return getString(R.string.method_karachi);
        }
        return getString(R.string.method_mwl);
    }

    private String asrLabel(String value) {
        return "HANAFI".equals(value)
                ? getString(R.string.asr_hanafi)
                : getString(R.string.asr_standard);
    }

    private String formatPrayerTimes(List<PrayerTime> times) {
        StringBuilder result = new StringBuilder();
        String[] names = getResources().getStringArray(
                R.array.prayer_names
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(
                FormatStyle.SHORT
        ).withLocale(Locale.getDefault());

        for (PrayerTime value : times) {
            if (result.length() > 0) {
                result.append("\n");
            }
            String name = value.prayer.ordinal() < names.length
                    ? names[value.prayer.ordinal()]
                    : value.prayer.name();
            result.append(String.format(
                    Locale.getDefault(),
                    "%-9s %s%s",
                    name,
                    formatter.format(value.time),
                    value.available ? "" : "  •"
            ));
        }
        return result.toString();
    }

    private void applyDeviceLocation() {
        LocationProfile device =
                DeviceLocationProfileProvider.lastKnown(this);

        if (device == null) {
            Toast.makeText(
                    this,
                    getString(R.string.islamic_location_unavailable),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        location = device;
        locationStore.save(location);
        refreshCalculations();

        if (settings.enabled && !rescheduleReminders()) {
            Toast.makeText(
                    this,
                    getString(R.string.islamic_adhan_inexact),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean rescheduleReminders() {
        boolean exact = true;
        for (int i = 0; i < 7; i++) {
            exact &= SalahAlarmScheduler.scheduleDay(
                    this,
                    LocalDate.now().plusDays(i),
                    location
            );
        }
        return exact;
    }

    private void toggleAdhan() {
        boolean enabled = !settings.enabled;
        settings = new AdhanSettings(
                enabled,
                enabled,
                settings.calculationSettings
        );
        settingsStore.save(settings);
        if (enabled) {
            locationStore.save(location);
        }

        if (!enabled) {
            for (int i = 0; i < 7; i++) {
                SalahAlarmScheduler.cancelDay(
                        this,
                        LocalDate.now().plusDays(i)
                );
            }
        } else {
            if (!rescheduleReminders()) {
                Toast.makeText(
                        this,
                        getString(R.string.islamic_adhan_inexact),
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        adhanButton.setText(getString(enabled
                ? R.string.islamic_adhan_disable
                : R.string.islamic_adhan_enable));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != REQUEST_LOCATION) {
            return;
        }

        if (grantResults.length > 0
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                || (grantResults.length > 1
                && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
            applyDeviceLocation();
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.islamic_location_denied),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
