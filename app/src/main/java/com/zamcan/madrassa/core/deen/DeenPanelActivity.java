package com.zamcan.madrassa.core.deen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zamcan.madrassa.R;
import com.zamcan.madrassa.core.LanguageManager;
import com.zamcan.madrassa.core.calendar.CalendarActivity;
import com.zamcan.madrassa.core.calendar.EduNoorCalendars;
import com.zamcan.madrassa.core.calendar.EduNoorDateFormatter;
import com.zamcan.madrassa.core.salat.SalahTimes;
import com.zamcan.madrassa.core.sound.EduNoorSounds;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Deen services panel - opened from the Kaaba mark on the landing
 * page. Today's salat timetable (offline, MWL, from the saved
 * location), the Qibla bearing toward the Holy Kaaba, adhana
 * settings, the location that drives the timetable, and the
 * calendar settings (Hijri display adjustment + calendar page).
 */
public class DeenPanelActivity extends Activity {

    private EditText latField;
    private EditText lonField;
    private TextView locationMessage;
    private TextView adhanToggle;
    private TextView hijriValue;

    private int dp(float value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    private TextView text(String value, float sizeSp,
                          int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );
        return view;
    }

    @Override
    protected void attachBaseContext(
            android.content.Context newBase
    ) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int background = getColor(R.color.edunoor_background);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        /*
         * HEADER
         */
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(7), dp(16), dp(7));

        TextView back = text("‹", 30,
                getColor(R.color.edunoor_clay), false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> finish());
        header.addView(
                back,
                new LinearLayout.LayoutParams(dp(42), dp(48))
        );

        LinearLayout headerWords = new LinearLayout(this);
        headerWords.setOrientation(LinearLayout.VERTICAL);
        headerWords.addView(
                text(getString(R.string.deen_title), 16,
                        getColor(R.color.edunoor_walnut), true),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        headerWords.addView(
                text(getString(R.string.deen_subtitle), 10.5f,
                        getColor(R.color.edunoor_muted), false),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        header.addView(
                headerWords,
                new LinearLayout.LayoutParams(0, -1, 1)
        );

        header.addView(
                kaabaIcon(),
                new LinearLayout.LayoutParams(dp(30), dp(30))
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(6), dp(16), dp(20));

        content.addView(salatCard(), cardParams());
        content.addView(qiblaCard(), spacedParams());
        content.addView(adhanCard(), spacedParams());
        content.addView(locationCard(), spacedParams());
        content.addView(calendarCard(), spacedParams());

        scroll.addView(
                content,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1)
        );

        setContentView(root);
    }

    private void refreshAll() {
        /*
         * Preference-driven sections rebuild on a fresh pass;
         * the platform recreates the activity with the wrapped
         * locale intact (attachBaseContext).
         */
        recreate();
    }

    private LinearLayout.LayoutParams cardParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams spacedParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        return params;
    }

    private View kaabaIcon() {

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.faith_kaaba);
        return icon;
    }

    /*
     * =========================================================
     * SALAT TIMETABLE - today, from the saved location.
     * =========================================================
     */
    private View salatCard() {

        LinearLayout card = sectionCard();

        LocalDate today = DeenPrefs.adjustedToday(this);

        SalahTimes times = new SalahTimes(
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                DeenPrefs.latitude(this),
                DeenPrefs.longitude(this),
                DeenPrefs.timeZoneOffset(this)
        );

        double now = nowDecimalHours();
        int next = nextPrayer(times, now);

        String[] names = {
                getString(R.string.prayer_fajr),
                getString(R.string.prayer_sunrise),
                getString(R.string.prayer_dhuhr),
                getString(R.string.prayer_asr),
                getString(R.string.prayer_maghrib),
                getString(R.string.prayer_isha)
        };

        LinearLayout headingRow = new LinearLayout(this);
        headingRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView heading = text(
                "☾  " + getString(R.string.deen_salat), 14,
                getColor(R.color.edunoor_walnut), true);

        TextView hijriDate = text(
                EduNoorDateFormatter.formatHijri(
                        EduNoorCalendars.toHijri(today),
                        getResources().getStringArray(
                                R.array.calendar_hijri_months)),
                10, getColor(R.color.edunoor_gold_deep), true);
        hijriDate.setGravity(Gravity.END);

        headingRow.addView(
                heading,
                new LinearLayout.LayoutParams(0, -2, 1)
        );
        headingRow.addView(
                hijriDate,
                new LinearLayout.LayoutParams(-2, -2)
        );

        card.addView(
                headingRow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        int index = 0;
        for (String name : names) {

            boolean isNext = index == next;

            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(7), 0, dp(7));

            TextView nameView = text(
                    (isNext ? "• " : "") + name,
                    isNext ? 13.5f : 12.5f,
                    getColor(isNext
                            ? R.color.edunoor_gold_deep
                            : R.color.edunoor_ink_soft),
                    isNext);
            nameView.setGravity(Gravity.CENTER_VERTICAL);

            TextView timeView = text(
                    SalahTimes.format(times.hours[index]),
                    isNext ? 14 : 13,
                    getColor(isNext
                            ? R.color.edunoor_gold_deep
                            : R.color.edunoor_ink),
                    true);
            timeView.setGravity(Gravity.END);

            row.addView(
                    nameView,
                    new LinearLayout.LayoutParams(0, -2, 1)
            );
            row.addView(
                    timeView,
                    new LinearLayout.LayoutParams(-2, -2)
            );

            card.addView(
                    row,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
            );

            index++;
        }

        TextView caption = text(
                getString(R.string.deen_salat_caption), 10,
                getColor(R.color.edunoor_muted), false);
        LinearLayout.LayoutParams captionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        captionParams.topMargin = dp(4);
        card.addView(caption, captionParams);

        return card;
    }

    private double nowDecimalHours() {
        java.util.Calendar calendar =
                java.util.Calendar.getInstance();
        return calendar.get(java.util.Calendar.HOUR_OF_DAY)
                + calendar.get(java.util.Calendar.MINUTE) / 60.0;
    }

    private int nextPrayer(SalahTimes times, double now) {

        int[] prayers = {
                SalahTimes.FAJR, SalahTimes.DHUHR,
                SalahTimes.ASR, SalahTimes.MAGHRIB,
                SalahTimes.ISHA
        };

        int next = SalahTimes.FAJR;
        double best = Double.MAX_VALUE;

        for (int prayer : prayers) {
            double wait = times.hours[prayer] - now;
            if (wait >= 0 && wait < best) {
                best = wait;
                next = prayer;
            }
        }

        return next;
    }

    /*
     * =========================================================
     * QIBLA - great-circle bearing to the Holy Kaaba.
     * =========================================================
     */
    private View qiblaCard() {

        LinearLayout card = sectionCard();
        card.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView heading = text(
                "۞  " + getString(R.string.deen_qibla), 14,
                getColor(R.color.edunoor_walnut), true);
        heading.setGravity(Gravity.CENTER);
        card.addView(
                heading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        android.widget.ImageView rose =
                new android.widget.ImageView(this);
        rose.setImageResource(R.drawable.faith_qibla);
        rose.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        double bearing = SalahTimes.qiblaBearing(
                DeenPrefs.latitude(this),
                DeenPrefs.longitude(this));

        rose.setRotation((float) -bearing);

        card.addView(
                rose,
                new LinearLayout.LayoutParams(dp(130), dp(130))
        );

        TextView value = text(
                getString(R.string.deen_qibla_value,
                        String.format(Locale.ROOT, "%.1f", bearing),
                        cardinalName(bearing)),
                13, getColor(R.color.edunoor_gold_deep), true);
        value.setGravity(Gravity.CENTER);
        card.addView(
                value,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        TextView caption = text(
                getString(R.string.deen_qibla_caption), 10,
                getColor(R.color.edunoor_muted), false);
        caption.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams captionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        captionParams.topMargin = dp(4);
        card.addView(caption, captionParams);

        return card;
    }

    private String cardinalName(double bearing) {

        String[] names = {
                getString(R.string.deen_dir_n),
                getString(R.string.deen_dir_ne),
                getString(R.string.deen_dir_e),
                getString(R.string.deen_dir_se),
                getString(R.string.deen_dir_s),
                getString(R.string.deen_dir_sw),
                getString(R.string.deen_dir_w),
                getString(R.string.deen_dir_nw)
        };

        return names[
                ((int) Math.floor(
                        ((bearing % 360) + 360) % 360 / 45.0 + 0.5)) % 8];
    }

    /*
     * =========================================================
     * ADHANA SETTINGS
     * =========================================================
     */
    private View adhanCard() {

        LinearLayout card = sectionCard();

        TextView heading = text(
                getString(R.string.deen_adhan), 14,
                getColor(R.color.edunoor_walnut), true);
        card.addView(
                heading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        TextView caption = text(
                getString(R.string.deen_adhan_caption), 10.5f,
                getColor(R.color.edunoor_muted), false);
        LinearLayout.LayoutParams captionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        captionParams.topMargin = dp(3);
        card.addView(caption, captionParams);

        adhanToggle = new TextView(this);
        adhanToggle.setGravity(Gravity.CENTER);
        adhanToggle.setTextSize(12.5f);
        adhanToggle.setTypeface(
                Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams toggleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(38));
        toggleParams.topMargin = dp(10);
        adhanToggle.setOnClickListener(v -> {

            boolean newState = !DeenPrefs.isAdhanOn(this);
            DeenPrefs.setAdhanOn(this, newState);
            styleAdhanToggle();

            if (newState) {
                EduNoorSounds.tap(this);
            } else {
                EduNoorSounds.delete(this);
            }
        });

        styleAdhanToggle();
        card.addView(adhanToggle, toggleParams);

        return card;
    }

    private void styleAdhanToggle() {

        boolean on = DeenPrefs.isAdhanOn(this);

        adhanToggle.setText(getString(on
                ? R.string.deen_adhan_on
                : R.string.deen_adhan_off));

        GradientDrawable pill = new GradientDrawable();
        pill.setColor(getColor(on
                ? R.color.edunoor_gold_soft
                : R.color.edunoor_surface));
        pill.setStroke(dp(1), getColor(on
                ? R.color.edunoor_gold_deep
                : R.color.edunoor_border));
        pill.setCornerRadius(dp(9));
        adhanToggle.setBackground(pill);

        adhanToggle.setTextColor(getColor(on
                ? R.color.edunoor_gold_deep
                : R.color.edunoor_muted));
        adhanToggle.setAlpha(on ? 1f : 0.75f);
    }

    /*
     * =========================================================
     * LOCATION - drives the timetable; default Dar es Salaam.
     * =========================================================
     */
    private View locationCard() {

        LinearLayout card = sectionCard();

        TextView heading = text(
                getString(R.string.deen_location), 14,
                getColor(R.color.edunoor_walnut), true);
        card.addView(
                heading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        TextView caption = text(
                getString(R.string.deen_location_caption), 10.5f,
                getColor(R.color.edunoor_muted), false);
        LinearLayout.LayoutParams captionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        captionParams.topMargin = dp(3);
        card.addView(caption, captionParams);

        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.HORIZONTAL);

        latField = locationField();
        lonField = locationField();

        latField.setHint(getString(R.string.deen_lat_hint));
        lonField.setHint(getString(R.string.deen_lon_hint));

        latField.setText(String.format(Locale.ROOT, "%.4f",
                DeenPrefs.latitude(this)));
        lonField.setText(String.format(Locale.ROOT, "%.4f",
                DeenPrefs.longitude(this)));

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(0, dp(44), 1);
        leftParams.rightMargin = dp(8);

        fields.addView(latField, leftParams);
        fields.addView(lonField,
                new LinearLayout.LayoutParams(0, dp(44), 1));

        LinearLayout.LayoutParams fieldsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        fieldsParams.topMargin = dp(10);

        card.addView(fields, fieldsParams);

        locationMessage = text("", 11,
                getColor(R.color.edunoor_clay), false);

        TextView save = new TextView(this);
        save.setText(getString(R.string.deen_save));
        save.setGravity(Gravity.CENTER);
        save.setTextSize(12.5f);
        save.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        save.setTextColor(getColor(R.color.edunoor_gold_deep));

        GradientDrawable chip = new GradientDrawable();
        chip.setColor(getColor(R.color.edunoor_gold_soft));
        chip.setStroke(dp(1), getColor(R.color.edunoor_gold_deep));
        chip.setCornerRadius(dp(9));
        save.setBackground(chip);

        save.setOnClickListener(v -> saveLocation());

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(40));
        saveParams.topMargin = dp(10);

        card.addView(save, saveParams);

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = dp(6);

        card.addView(locationMessage, messageParams);

        return card;
    }

    private EditText locationField() {

        EditText field = new EditText(this);
        field.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED);
        field.setTextSize(13);
        field.setTextColor(getColor(R.color.edunoor_ink));
        field.setHintTextColor(getColor(R.color.edunoor_muted));

        GradientDrawable outline = new GradientDrawable();
        outline.setColor(getColor(R.color.edunoor_surface));
        outline.setStroke(dp(1), getColor(R.color.edunoor_border));
        outline.setCornerRadius(dp(9));
        field.setBackground(outline);
        field.setPadding(dp(12), 0, dp(12), 0);

        return field;
    }

    private void saveLocation() {

        String latText = latField.getText().toString().trim();
        String lonText = lonField.getText().toString().trim();

        try {
            double lat = Double.parseDouble(latText);
            double lon = Double.parseDouble(lonText);

            if (lat < -90 || lat > 90
                    || lon < -180 || lon > 180) {
                throw new NumberFormatException();
            }

            DeenPrefs.setLocation(this, lat, lon);
            locationMessage.setTextColor(
                    getColor(R.color.edunoor_gold_deep));
            locationMessage.setText(
                    getString(R.string.deen_saved));

            EduNoorSounds.tap(this);
            refreshAll();

        } catch (Exception error) {
            locationMessage.setTextColor(
                    getColor(R.color.edunoor_clay));
            locationMessage.setText(
                    getString(R.string.deen_invalid_location));
        }
    }

    /*
     * =========================================================
     * CALENDAR SETTINGS
     * =========================================================
     */
    private View calendarCard() {

        LinearLayout card = sectionCard();

        TextView heading = text(
                getString(R.string.deen_calendar), 14,
                getColor(R.color.edunoor_walnut), true);
        card.addView(
                heading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        LinearLayout stepper = new LinearLayout(this);
        stepper.setGravity(Gravity.CENTER_VERTICAL);
        stepper.setPadding(0, dp(8), 0, dp(8));

        TextView minus = stepChip("−");
        minus.setOnClickListener(v -> stepAdjust(-1));

        TextView plus = stepChip("+");
        plus.setOnClickListener(v -> stepAdjust(1));

        hijriValue = new TextView(this);
        hijriValue.setGravity(Gravity.CENTER);
        hijriValue.setTextSize(13);
        hijriValue.setTypeface(
                Typeface.DEFAULT, Typeface.BOLD);
        hijriValue.setTextColor(
                getColor(R.color.edunoor_gold_deep));

        stepper.addView(
                minus,
                new LinearLayout.LayoutParams(dp(40), dp(36))
        );
        stepper.addView(
                hijriValue,
                new LinearLayout.LayoutParams(0, dp(36), 1)
        );
        stepper.addView(
                plus,
                new LinearLayout.LayoutParams(dp(40), dp(36))
        );

        card.addView(
                stepper,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        TextView caption = text(
                getString(R.string.deen_hijri_caption), 10.5f,
                getColor(R.color.edunoor_muted), false);
        LinearLayout.LayoutParams captionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        captionParams.topMargin = dp(2);
        card.addView(caption, captionParams);

        TextView open = new TextView(this);
        open.setText(getString(R.string.deen_open_calendar));
        open.setGravity(Gravity.CENTER);
        open.setTextSize(12.5f);
        open.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        open.setTextColor(getColor(R.color.edunoor_gold_deep));

        GradientDrawable chip = new GradientDrawable();
        chip.setColor(getColor(R.color.edunoor_surface));
        chip.setStroke(dp(1), getColor(R.color.edunoor_border));
        chip.setCornerRadius(dp(9));
        open.setBackground(chip);

        open.setOnClickListener(v -> {
            EduNoorSounds.page(this);
            startActivity(new Intent(this, CalendarActivity.class));
        });

        LinearLayout.LayoutParams openParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(40));
        openParams.topMargin = dp(10);

        card.addView(open, openParams);

        refreshHijriValue();

        return card;
    }

    private TextView stepChip(String symbol) {

        TextView chip = new TextView(this);
        chip.setText(symbol);
        chip.setGravity(Gravity.CENTER);
        chip.setTextSize(18);
        chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        chip.setTextColor(getColor(R.color.edunoor_gold_deep));

        GradientDrawable outline = new GradientDrawable();
        outline.setColor(getColor(R.color.edunoor_surface));
        outline.setStroke(dp(1), getColor(R.color.edunoor_border));
        outline.setCornerRadius(dp(9));
        chip.setBackground(outline);

        return chip;
    }

    private void stepAdjust(int direction) {

        int updated = DeenPrefs.hijriAdjust(this) + direction;

        if (updated < -2 || updated > 2) {
            return;
        }

        DeenPrefs.setHijriAdjust(this, updated);
        refreshHijriValue();
        EduNoorSounds.tap(this);
    }

    private void refreshHijriValue() {

        int adjust = DeenPrefs.hijriAdjust(this);

        hijriValue.setText(
                getString(R.string.deen_hijri_days, adjust));
    }

    private LinearLayout sectionCard() {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(13), dp(16), dp(13));
        card.setBackground(getDrawable(R.drawable.edunoor_canvas));
        return card;
    }
}
